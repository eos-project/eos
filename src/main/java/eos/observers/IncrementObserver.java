package eos.observers;

import eos.type.EosKey;

/**
 * Interface, describing observer for increment events
 */
public interface IncrementObserver extends Observer
{
    void report(Event event);

    /**
     * Increment event
     */
    public static class Event extends ObservingEvent
    {
        final long delta;

        /**
         * Constructor
         *
         * @param key   Eos key
         * @param delta Delta
         */
        public Event(EosKey key, long delta) {
            super(key);
            if (!key.schemaEquals(EosKey.Schema.inc)) {
                throw new IllegalArgumentException("Expecting increment schema, but received " + key);
            }
            this.delta = delta;
        }

        /**
         * @return Increment delta
         */
        public long getDelta() {
            return delta;
        }
    }
}
