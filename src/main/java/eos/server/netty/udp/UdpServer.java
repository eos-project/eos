package eos.server.netty.udp;

import eos.EosController;
import eos.EosRegistry;
import eos.server.*;
import eos.type.EosKey;
import eos.type.Logger;
import eos.type.LongIncrement;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetAddress;

public class UdpServer implements Runnable
{
    final String host;
    final int port;
    final StringEosControllerAdapter adapter;
    final Logger logger;
    final LongIncrement udpServerRequests;
    final LongIncrement udpServerFailures;

    public UdpServer(
        String host,
        int port,
        EosRegistry internalMetrics,
        EosController metricController
    ) throws Exception
    {
        this.host             = host;
        this.port             = port;
        // Internal metrics
        this.logger           = (Logger) internalMetrics.take(
                new EosKey(
                        EosKey.Schema.log,
                        "eos.core.server.udp",
                        InetAddress.getLocalHost().getHostName(),
                        "eos"
                )
        );
        udpServerRequests = (LongIncrement) internalMetrics.take(
                new EosKey(
                        EosKey.Schema.inc,
                        "eos.core.server.udp.requests",
                        InetAddress.getLocalHost().getHostName(),
                        "eos"
                )
        );
        udpServerFailures = (LongIncrement) internalMetrics.take(
            new EosKey(
                EosKey.Schema.inc,
                "eos.core.server.udp.failures",
                InetAddress.getLocalHost().getHostName(),
                "eos"
            )
        );

        // Building adapter
        adapter = new StringEosControllerAdapter(metricController, metricController.getTokenRepository());
        this.logger.log("New instance of UdpServer created");
    }

    @Override
    public void run() {
        logger.log("Starting UDP server at " + host + " port " + port);

        final EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new UdpHandler());

            b.bind(host, port).sync().channel().closeFuture().sync();
            logger.log("UDP server online at " + host + " port " + port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }

    class UdpHandler extends SimpleChannelInboundHandler<DatagramPacket>
    {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            String content = msg.content().toString(CharsetUtil.UTF_8);

            // Resolving key
            try {
                udpServerRequests.inc();
                adapter.process(content.split("\n"));
            } catch (Exception e) {
                udpServerFailures.inc();
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
    }
}
