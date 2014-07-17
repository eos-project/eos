package eos.server.netty.ws;

import eos.observers.LoggersObserver;
import eos.observers.Observer;
import eos.observers.ObservingEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WsObserver implements Observer
{
    final ChannelHandlerContext ctx;

    public WsObserver(ChannelHandlerContext ctx) {
        if (ctx == null) {
            throw new NullPointerException();
        }
        this.ctx = ctx;
    }

    @Override
    public void report(ObservingEvent event) {
        if (event == null) return;

        if (event instanceof LoggersObserver.Event) {
            TextWebSocketFrame frame = new TextWebSocketFrame(
                event.getKey().toString() + "\n" + ((LoggersObserver.Event) event).getLine()
            );

            this.ctx.writeAndFlush(frame);
        }
    }
}
