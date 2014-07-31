package eos.filters;

import eos.type.EosKey;
import eos.type.KeyFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Performs filtering using extended multi-tag logic
 */
public class MultiTagFilter implements KeyFilter
{
    final List<Rule> rules;

    /**
     * Constructor
     *
     * @param rules Varargs list of rules to use
     */
    public MultiTagFilter(Rule... rules) {
        this(Arrays.asList(rules));
    }

    /**
     * Constructor
     *
     * @param rules List of rules to use
     */
    public MultiTagFilter(List<Rule> rules) {
        if (rules == null || rules.size() == 0) {
            throw new NullPointerException("Empty rules list received");
        }
        this.rules = rules;
    }

    @Override
    public boolean matches(EosKey key) {
        if (key == null) {
            return false;
        }
        for (Rule rule : rules) {
            if (rule.operation == Operation.MUST && !key.hasTag(rule.tag)) {
                // Required tag is missing
                return false;
            }
            if (rule.operation == Operation.MUST_NOT && key.hasTag(rule.tag)) {
                // Ignored tag is present
                return false;
            }
        }
        return true;
    }

    /**
     * Matching rule, used in key filter
     */
    public static class Rule
    {
        final String tag;
        final Operation operation;

        /**
         * Constructor
         *
         * @param tag       Tag to match
         * @param operation Operation to use
         */
        public Rule(String tag, Operation operation) {
            if (tag == null || operation == null) {
                throw new NullPointerException();
            }
            this.tag = tag;
            this.operation = operation;
        }
    }

    /**
     * Operation type, used in key filter
     */
    public static enum Operation
    {
        MUST, MUST_NOT
    }
}
