package eos.filters;

import eos.type.EosKey;
import eos.type.KeyFilter;

/**
 * Filter, that returns true only if key have tag
 */
public class SingleTagFilter implements KeyFilter
{
    final String tag;

    /**
     * Constructor
     * @param tag Tag to match
     */
    public SingleTagFilter(String tag) {
        if (tag == null) {
            throw new NullPointerException("tag");
        }
        this.tag = tag;
    }

    @Override
    public boolean matches(EosKey key) {
        return key.hasTag(tag);
    }
}
