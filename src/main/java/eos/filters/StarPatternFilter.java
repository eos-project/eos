package eos.filters;

import eos.type.EosKey;
import eos.type.KeyFilter;

import java.util.regex.Pattern;

/**
 * Filter, using regular expression to match
 * Replaces * to . in provided string pattern
 */
public class StarPatternFilter implements KeyFilter {
    Pattern pattern;

    /**
     * Constructor
     *
     * @param stringPattern String pattern
     */
    public StarPatternFilter(String stringPattern)
    {
        if (stringPattern != null && !stringPattern.equals("*")) {
            pattern = Pattern.compile(stringPattern.replaceAll("\\*", "\\.*"));
        } else {
            pattern = null;
        }
    }

    @Override
    public boolean matches(EosKey key) {
        return pattern == null || pattern.matcher(key.toString()).find();
    }

}
