package eos.observers;

import eos.type.EosKey;

/**
 * Base class for events
 */
public class ObservingEvent
{
    private final EosKey key;

    /**
     * Constructor
     *
     * @param key Eos key, this event bound to
     */
    public ObservingEvent(EosKey key) {
        if (key == null) {
            throw new NullPointerException("Provided key is null");
        }
        this.key = key;
    }

    /**
     * @return Eos key for event
     */
    public EosKey getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
