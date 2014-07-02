package eos.filters;

import eos.type.KeyFilter;

/**
 * Creates filters
 */
public class FilterFactory
{
    /**
     * This is stub method
     *
     * @param pattern Pattern for filter
     * @return Created filter or null
     */
    public KeyFilter getFilter(String pattern)
    {
        if (pattern == null || pattern.length() == 0) {
            return null;
        }
        if (pattern.startsWith("+:")) {
            return new SingleTagFilter(pattern.substring(2));
        } else if (pattern.startsWith(":")) {
            return new SingleTagFilter(pattern.substring(1));
        }
        return new StarPatternFilter(pattern);
    }
}
