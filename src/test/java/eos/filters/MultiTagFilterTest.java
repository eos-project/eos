package eos.filters;

import eos.type.EosKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class MultiTagFilterTest
{
    @Test(expected = NullPointerException.class)
    public void testEmptyList()
    {
        new MultiTagFilter(new ArrayList<MultiTagFilter.Rule>());
    }

    @Test
    public void testLogic()
    {
        EosKey
            x1 = new EosKey(EosKey.Schema.log, "x"), // No tags
            x2 = new EosKey(EosKey.Schema.log, "x", "foo"),
            x3 = new EosKey(EosKey.Schema.log, "x", "bar"),
            x4 = new EosKey(EosKey.Schema.log, "x", "foo", "bar");

        MultiTagFilter filter;

        filter = new MultiTagFilter(new MultiTagFilter.Rule("foo", MultiTagFilter.Operation.MUST));
        Assert.assertFalse(filter.matches(x1));
        Assert.assertTrue(filter.matches(x2));
        Assert.assertFalse(filter.matches(x3));
        Assert.assertTrue(filter.matches(x4));

        filter = new MultiTagFilter(new MultiTagFilter.Rule("foo", MultiTagFilter.Operation.MUST_NOT));
        Assert.assertTrue(filter.matches(x1));
        Assert.assertFalse(filter.matches(x2));
        Assert.assertTrue(filter.matches(x3));
        Assert.assertFalse(filter.matches(x4));

        filter = new MultiTagFilter(
            new MultiTagFilter.Rule("foo", MultiTagFilter.Operation.MUST_NOT),
            new MultiTagFilter.Rule("bar", MultiTagFilter.Operation.MUST)
        );
        Assert.assertFalse(filter.matches(x1));
        Assert.assertFalse(filter.matches(x2));
        Assert.assertTrue(filter.matches(x3));
        Assert.assertFalse(filter.matches(x4));

        filter = new MultiTagFilter(
                new MultiTagFilter.Rule("foo", MultiTagFilter.Operation.MUST_NOT),
                new MultiTagFilter.Rule("baz", MultiTagFilter.Operation.MUST_NOT),
                new MultiTagFilter.Rule("bar", MultiTagFilter.Operation.MUST)
        );
        Assert.assertFalse(filter.matches(x1));
        Assert.assertFalse(filter.matches(x2));
        Assert.assertTrue(filter.matches(x3));
        Assert.assertFalse(filter.matches(x4));
    }
}
