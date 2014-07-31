package eos.observers;

import eos.type.EosKey;

/**
 * Interface, describing observer for logging events
 */
public interface LoggersObserver extends Observer
{
    void report(Event event);

    /**
     * Log event
     */
    public static class Event extends ObservingEvent
    {
        final String line;

        /**
         * Constructor
         *
         * @param key  Eos key
         * @param line Log line
         */
        public Event(EosKey key, String line) {
            super(key);
            if (!key.schemaEquals(EosKey.Schema.log)) {
                throw new IllegalArgumentException("Expecting logging schema, but received " + key);
            }
            this.line = line;
        }

        /**
         * @return Log line
         */
        public String getLine() {
            return line;
        }
    }
}
