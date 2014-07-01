package eos.observers;

import eos.type.EosKey;

public interface IncrementObserver extends Observer
{
    void report(Event event);

    public static class Event extends ObservingEvent
    {
        final long delta;

        public Event(EosKey key, long delta) {
            super(key);
            this.delta = delta;
        }

        public long getDelta() {
            return delta;
        }
    }
}
