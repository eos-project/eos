package eos.server.netty.tcp;

import eos.observers.Observer;
import eos.observers.ObservingEvent;
import eos.server.netty.tcp.packet.Event;
import eos.type.KeyFilter;
import io.netty.channel.ChannelHandlerContext;

public class TcpReplicaObserver implements Observer
{
    final KeyFilter filter;
    final ChannelHandlerContext ctx;

    public TcpReplicaObserver(final ChannelHandlerContext ctx, final KeyFilter filter) {
        this.filter = filter;
        this.ctx    = ctx;
    }

    @Override
    public void report(ObservingEvent event) {
        if (filter != null && !filter.matches(event.getKey())) {
            // Not matches
            return;
        }

        // Creating event
        Event e = Event.fromObserverving(event);

        // Sending
        ctx.writeAndFlush(e);
    }
}
