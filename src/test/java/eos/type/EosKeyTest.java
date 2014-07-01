package eos.type;

import org.junit.Assert;
import org.junit.Test;

public class EosKeyTest
{
    @Test
    public void testSuccess()
    {
        EosKey e = EosKey.parse("log://test.key");
        Assert.assertEquals(EosKey.Schema.log, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertFalse(e.hasServer());
        Assert.assertFalse(e.hasTags());

        e = EosKey.parse("inc://test.key@localhost");
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertTrue(e.hasServer());
        Assert.assertFalse(e.hasTags());

        e = EosKey.parse("inc://test.key@localhost:tag1");
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertTrue(e.hasServer());
        Assert.assertTrue(e.hasTags());
        Assert.assertEquals(1, e.tags.length);

        e = EosKey.parse("inc://test.key:tag1@localhost:tag2:tagN");
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertTrue(e.hasServer());
        Assert.assertTrue(e.hasTags());
        Assert.assertEquals(3, e.tags.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleServers()
    {
        EosKey.parse("log://test@localhost@remote");
    }
}
