package eos.server.netty.ws;

import eos.EosRegistry;
import eos.filters.FilterFactory;
import eos.observers.Observer;
import eos.observers.ObservingPool;
import eos.type.EosKey;
import eos.type.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

public class WsServer implements Runnable
{
    final String host;
    final int port;
    final ObservingPool pool;
    final FilterFactory filterFactory;
    final EosKey welcomeKey;

    final Logger logger;

    public WsServer(String host, int port, EosRegistry internalMetrics, ObservingPool pool) throws Exception
    {
        this.host = host;
        this.port = port;
        this.pool = pool;
        this.filterFactory = new FilterFactory();

        // Internal metrics
        this.logger = (Logger) internalMetrics.take(new EosKey(EosKey.Schema.log, "eos.core.server.ws"));

        // Welcome key
        this.welcomeKey = new EosKey(EosKey.Schema.log, "eos.core.server.ws.welcome");

        this.logger.log("New instance of websocket server created");
    }

    @Override
    public void run() {
        logger.log("Starting websocket server at " + host + " port " + port);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(25);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new Initializer());

            b.bind(host, port).sync().channel().closeFuture().sync();
            logger.log("Websocket server online at " + host + " port " + port);
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
            p.addLast(new HttpServerCodec());
            p.addLast(new HttpObjectAggregator(65536));
            p.addLast("handler", new Handler());
        }
    }

    class Handler extends SimpleChannelInboundHandler<Object>
    {
        private WebSocketServerHandshaker handshaker;
        private Observer observer;

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            logger.log("Client connected " + ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            if (observer != null) {
                pool.unregister(observer);
                observer = null;
            }
            logger.log("Channel unregistered");
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.log("Exception " + cause.getClass() + " " + cause.getMessage());
            if (observer != null) {
                pool.unregister(observer);
                observer = null;
            }
            ctx.close();
        }

        private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, false
            );
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
                observer = new WsObserver(ctx);
                pool.register(observer);
            }
        }

        private String getWebSocketLocation(FullHttpRequest req) {
            return "ws://" + req.headers().get(HttpHeaders.Names.HOST) + "/";
        }
    }
}
