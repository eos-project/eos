package eos.observers;

import eos.type.EosKey;

public interface LoggersObserver extends Observer
{
    void report(Event event);

    public static class Event extends ObservingEvent
    {
        final String line;

        public Event(EosKey key, String line) {
            super(key);
            this.line = line;
        }

        public String getLine() {
            return line;
        }
    }
}
