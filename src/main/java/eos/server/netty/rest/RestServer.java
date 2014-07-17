package eos.server.netty.rest;

import eos.EosController;
import eos.EosRegistry;
import eos.render.RenderableList;
import eos.server.EntryNotFoundException;
import eos.server.WrongRequestException;
import eos.server.WrongTokenException;
import eos.type.EosKey;
import eos.type.EosKeyResolver;
import eos.type.Logger;
import eos.type.LongIncrement;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestServer implements Runnable
{
    static final Pattern urlApi       = Pattern.compile("/api/(.*)");
    static final Pattern urlGetMetric = Pattern.compile("/api/get/(.*)");
    static final Pattern urlGetList   = Pattern.compile("/api/find/([^/]*)");

    final String host;
    final int port;
    final EosController metricController;
    final Logger logger;
    final LongIncrement restServerRequests;
    final EosKeyResolver resolver;

    public RestServer(String host, int port, EosRegistry internalMetrics, EosController metricController, EosKeyResolver resolver) throws Exception
    {
        this.host             = host;
        this.port             = port;
        this.metricController = metricController;
        this.resolver         = resolver;
        // Internal metrics
        this.logger             = (Logger) internalMetrics.take(new EosKey(EosKey.Schema.log, "eos.core.server.rest"));
        this.restServerRequests = (LongIncrement) internalMetrics.take(new EosKey(EosKey.Schema.inc, "eos.core.server.rest.requests"));
        this.logger.log("New instance of RestServer created");
    }

    @Override
    public void run() {
        logger.log("Starting REST server at " + host + " port " + port);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(25);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new Initializer());

            b.bind(host, port).sync().channel().closeFuture().sync();
            logger.log("REST server online at " + host + " port " + port);
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
            p.addLast("decoder", new HttpRequestDecoder());
            p.addLast("encoder", new HttpResponseEncoder());
            p.addLast("handler", new Handler());
        }
    }

    class Handler extends SimpleChannelInboundHandler<Object>
    {
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof HttpRequest) {
                restServerRequests.inc();
                HttpRequest req = (HttpRequest) msg;

                String token = req.headers().get("X-Auth-Token");
                String url   = req.getUri();

                RestResponse response;

                try {
                    Matcher m;
                    if (req.getMethod().equals(HttpMethod.GET)) {
                        // Get request

                        if ((m = urlGetMetric.matcher(url)).find()) {
                            String metricName = m.group(1);
                            response = new Success(
                                    metricController.getMetricRead(token, resolver.resolve(metricName)).export()
                            );
                        } else if ((m = urlGetList.matcher(url)).find()) {
                            String metricPattern = m.group(1);
                            response = new Success(
                                new RenderableList(metricController.findMetrics(metricPattern))
                            );
                        } else if (urlApi.matcher(url).find()) {
                            response = new ApiHelpResponse();
                        } else {
                            throw new WrongRequestException();
                        }
                    } else {
                        throw new WrongRequestException();
                    }
                } catch (WrongRequestException e) {
                    logger.log("Got hit on 400 with request unknown");
                    response = new Error(400, "request-unknown", "Request you made can not be handled by server", null);
                } catch (EntryNotFoundException e) {
                    logger.log("Got hit on 404 with metric unknown");
                    response = new Error(404, "metric-unknown", "Cannot find requested metric " + e.getName(), null);
                } catch (WrongTokenException e) {
                    logger.log("Got hit on 403 with token not allowed");
                    response = new Error(403, "token-not-allowed", "Your token is not valid", null);
                }

                // Sending response
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(response.getCode()),
                    Unpooled.copiedBuffer(response.toString(), CharsetUtil.UTF_8)
                );

                httpResponse.headers().set("Connection", "close");
                httpResponse.headers().set("X-Robots-Tag", "noindex, nofollow");
                httpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, response.getContentType());
                httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
                ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
