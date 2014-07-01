package eos.server.netty.tcp.packet;

import eos.observers.IncrementObserver;
import eos.observers.LoggersObserver;
import eos.observers.ObservingEvent;

import java.io.Serializable;

public class Event implements Serializable
{
    private static final long serialVersionUID = 1L;

    public String key;
    public String value;

    public static Event fromObserverving(ObservingEvent event)
    {
        Event e = new Event();
        e.key = event.getKey().toString();
        if (event instanceof IncrementObserver.Event) {
            e.value = "" +((IncrementObserver.Event) event).getDelta();
        } else if (event instanceof LoggersObserver.Event) {
            e.value = ((LoggersObserver.Event) event).getLine();
        } else {
            throw new IllegalArgumentException("Unsupported format");
        }

        return e;
    }
}
