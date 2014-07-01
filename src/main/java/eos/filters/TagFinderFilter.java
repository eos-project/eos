package eos.filters;

import eos.type.EosKey;
import eos.type.KeyFilter;

/**
 * Matches eos key against configured tag
 */
public class TagFinderFilter implements KeyFilter
{
    final String tag;

    /**
     * @param tag Tag name to match in keys
     */
    public TagFinderFilter(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean matches(EosKey key) {
        return key != null && tag != null && key.hasTag(tag);
    }
}
