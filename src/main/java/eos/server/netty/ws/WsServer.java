package eos.server.netty.ws;

import eos.EosRegistry;
import eos.filters.FilterFactory;
import eos.observers.Observer;
import eos.observers.ObservingPool;
import eos.realm.RealmDescriptor;
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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.concurrent.GenericProgressiveFutureListener;
import io.netty.util.concurrent.ProgressiveFuture;

import java.util.UUID;

public class WsServer implements Runnable
{
    final String host;
    final int port;
    final ObservingPool pool;
    final FilterFactory filterFactory;
    final EosKey welcomeKey;
    final RealmDescriptor realms;

    final Logger logger;

    public WsServer(String host, int port, EosRegistry internalMetrics, RealmDescriptor realms, ObservingPool pool) throws Exception
    {
        this.host = host;
        this.port = port;
        this.pool = pool;
        this.filterFactory = new FilterFactory();
        this.realms = realms;

        // Internal metrics
        this.logger = (Logger) internalMetrics.take(new EosKey("*",EosKey.Schema.log, "eos.core.server.ws"));

        // Welcome key
        this.welcomeKey = new EosKey("*", EosKey.Schema.log, "eos.core.server.ws.welcome");

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
        private UUID uuid = UUID.randomUUID();



        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            logger.log("Client connected " + ctx);
            ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    unregister();
                }
            });
        }


        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof TextWebSocketFrame) {
                String[] parts = ((TextWebSocketFrame) msg).text().split("\n");
                if (parts[0].equals("subscribe") && parts.length == 5) {
                    String realm = parts[1];
                    String nonce = parts[2];
                    String tag   = parts[3];
                    String hash  = parts[4];

                    if (realms.allowed(realm, nonce, tag, hash)) {
                        register(ctx, realm, tag);
                        ctx.writeAndFlush(new TextWebSocketFrame("connected" ));
                    } else {
                        ctx.writeAndFlush(new TextWebSocketFrame("error\nWrong realm"));
                    }
                } else {
                    ctx.writeAndFlush(new TextWebSocketFrame("error\nWrong subscribe packet"));
                }
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            unregister();
            logger.log("Channel unregistered");
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.log("Exception " + cause.getClass() + " " + cause.getMessage());
            unregister();
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
                TextWebSocketFrame frame = new TextWebSocketFrame("uuid\n" + uuid.toString());
                ctx.writeAndFlush(frame);
            }
        }

        void register(ChannelHandlerContext ctx, String realm, String tag) {
            observer = new WsObserver(ctx, realm, tag);
            pool.register(observer);
        }

        void unregister() {
            if (observer != null) {
                pool.unregister(observer);
                observer = null;
            }
        }

        private String getWebSocketLocation(FullHttpRequest req) {
            return "ws://" + req.headers().get(HttpHeaders.Names.HOST) + "/";
        }
    }
}
