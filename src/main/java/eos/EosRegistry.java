package eos;

import eos.observers.Observer;
import eos.type.EosEntry;
import eos.type.EosKey;
import eos.type.KeyFilter;

import java.util.List;

/**
 * Container for EOS entries (metrics, loggers, etc.)
 */
public interface EosRegistry extends Observer
{
    /**
     * Returns registry entry, or creates new one
     *
     * @param key Key
     * @return Entry
     */
    EosEntry take(EosKey key);

    /**
     * @param key Key to find
     * @return True, if registry contains metric or logger with provided key
     */
    boolean contains(EosKey key);

    /**
     * Returns list of keys, that matches a pattern
     *
     * @param filter Pattern to match
     * @return List of metrics
     */
    List<EosKey> getKeys(KeyFilter filter);
}
