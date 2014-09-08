package eos.server.netty.ws;

import eos.observers.LoggersObserver;
import eos.observers.Observer;
import eos.observers.ObservingEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WsObserver implements Observer
{
    final ChannelHandlerContext ctx;
    final String realm;
    final String tagFilter;

    public WsObserver(ChannelHandlerContext ctx, String realm, String tag) {
        if (ctx == null) {
            throw new NullPointerException();
        }
        this.realm = realm;
        this.ctx = ctx;
        this.tagFilter = tag == null || tag.trim().length() == 0 ? null : tag;
    }

    @Override
    public void report(ObservingEvent event) {
        if (event == null) return;

        if (!event.getKey().getRealm().equals(realm)) {
            return;
        }

        if (tagFilter != null && !event.getKey().hasTag(tagFilter)) {
            return;
        }

        if (event instanceof LoggersObserver.Event) {
            // Cutting realm from key
            String key = event.getKey().toString();
            key = key.substring(key.indexOf("+") + 1);

            TextWebSocketFrame frame = new TextWebSocketFrame(
               "log\n"
               + key + "\n"
               + ((LoggersObserver.Event) event).getLine()
            );

            this.ctx.writeAndFlush(frame);
        }
    }
}
