package eos.client.netty;

import eos.EosRegistry;
import eos.observers.IncrementObserver;
import eos.observers.LoggersObserver;
import eos.observers.ObservingEvent;
import eos.observers.ObservingPool;
import eos.server.netty.tcp.packet.Event;
import eos.server.netty.tcp.packet.Subscribe;
import eos.type.EosKey;
import eos.type.EosKeyResolver;
import eos.type.KeyFilter;
import eos.type.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.net.InetAddress;

/**
 * Tcp client, used for data replication
 */
public class TcpClient implements Runnable
{
    final ObservingPool pool;
    final Logger logger;

    final String host;
    final int port;
    final KeyFilter filter;
    final EosKeyResolver resolver;

    /**
     * Constructor
     *
     * @param internalMetrics Used to log internal events
     * @param pool            Observing pool
     * @param host            Hostname to connect
     * @param port            Port to connect
     * @param filter          Filter to use
     * @param resolver        Eos key resolver, used to convert string keys into objects
     * @throws Exception On any error
     */
    public TcpClient(
            EosRegistry internalMetrics,
            ObservingPool pool,
            String host,
            int port,
            KeyFilter filter,
            EosKeyResolver resolver
    ) throws Exception
    {
        this.host     = host;
        this.port     = port;
        this.pool     = pool;
        this.filter   = filter;
        this.resolver = resolver;
        this.logger   = (Logger) internalMetrics.take(
            new EosKey(
                EosKey.Schema.log,
                "eos.core.client.tcp",
                InetAddress.getLocalHost().getHostName(),
                "eos"
            )
        );
    }

    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);

        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast("decoder", new ObjectDecoder(ClassResolvers.softCachingResolver(getClass().getClassLoader())));
                p.addLast("encoder", new ObjectEncoder());
                p.addLast("handler", new Handler());
            }
        });

        try {
            b.connect(host, port).sync().channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Internal handler class
     */
    class Handler extends SimpleChannelInboundHandler<Object>
    {
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            logger.log("Packet received of type " + msg.getClass().toString());
            if (msg instanceof Event) {
                Event eMessage = (Event) msg;

                EosKey key = resolver.resolve(eMessage.key);
                if (key.schemaEquals(EosKey.Schema.log)) {
                    ObservingEvent oe = new LoggersObserver.Event(key, eMessage.value);
                    pool.report(oe);
                } else {
                    ObservingEvent oe = new IncrementObserver.Event(key, Long.parseLong(eMessage.value));
                    pool.report(oe);
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.log("Channel active");
            // Sending subscribe
            Subscribe sbs = new Subscribe();
            sbs.filterPattern = "*";

            ctx.writeAndFlush(sbs);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            logger.log("Channel unregistered");
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
