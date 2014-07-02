package eos.server.netty.tcp;

import eos.EosRegistry;
import eos.filters.FilterFactory;
import eos.observers.LoggersObserver;
import eos.observers.ObservingPool;
import eos.server.netty.tcp.packet.Subscribe;
import eos.type.EosKey;
import eos.type.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Server used for incoming client replica connections only
 */
public class TcpServer implements Runnable
{
    final String host;
    final int port;
    final ObservingPool pool;
    final FilterFactory filterFactory;
    final EosKey welcomeKey;

    final Logger logger;

    public TcpServer(String host, int port, EosRegistry internalMetrics, ObservingPool pool) throws Exception
    {
        this.host = host;
        this.port = port;
        this.pool = pool;
        this.filterFactory = new FilterFactory();

        // Internal metrics
        this.logger = (Logger) internalMetrics.take(new EosKey(EosKey.Schema.log, "eos.core.server.tcp", null));

        // Welcome key
        this.welcomeKey = new EosKey(EosKey.Schema.log, "eos.core.server.tcp.welcome", null);

        this.logger.log("New instance of TcpServer created");
    }

    @Override
    public void run() {
        logger.log("Starting TCP server at " + host + " port " + port);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(25);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new Initializer());

            b.bind(host, port).sync().channel().closeFuture().sync();
            logger.log("TCP server online at " + host + " port " + port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    class Initializer extends ChannelInitializer<SocketChannel>
    {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline p = ch.pipeline();
            p.addLast("decoder", new ObjectDecoder(ClassResolvers.softCachingResolver(getClass().getClassLoader())));
            p.addLast("encoder", new ObjectEncoder());
            p.addLast("handler", new Handler());
        }
    }

    class Handler extends SimpleChannelInboundHandler<Object>
    {
        TcpReplicaObserver observer;

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            logger.log("Client connected " + ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            logger.log("Received packet with type " + msg.getClass());
            if (msg instanceof Subscribe) {
                Subscribe sbsMessage = (Subscribe) msg;

                if (observer != null) {
                    // Unregister old
                    logger.log("Unregister old subscriber");
                    pool.unregister(observer);
                }

                // Creating new observer
                observer = new TcpReplicaObserver(ctx, filterFactory.getFilter(sbsMessage.filterPattern));

                // Registering
                pool.register(observer);

                // Sending welcome log packet
                observer.report(new LoggersObserver.Event(welcomeKey, "Connected"));
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            if (observer != null) {
                pool.unregister(observer);
            }
            logger.log("Channel unregistered");
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.log("Exception " + cause.getClass() + " " + cause.getMessage());
            if (observer != null) {
                pool.unregister(observer);
            }
            ctx.close();
        }
    }
}
