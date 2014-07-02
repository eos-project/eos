package eos.type;

import org.junit.Assert;
import org.junit.Test;

public class CachedEosKeyResolverTest
{
    @Test
    public void testResolveSuccess()
    {
        EosKey e = CachedEosKeyResolver.parse("log://test.key");
        Assert.assertEquals(EosKey.Schema.log, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertFalse(e.hasServer());
        Assert.assertFalse(e.hasTags());

        e = CachedEosKeyResolver.parse("inc://test.key@localhost");
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertTrue(e.hasServer());
        Assert.assertFalse(e.hasTags());

        e = CachedEosKeyResolver.parse("inc://test.key@localhost:tag1");
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertTrue(e.hasServer());
        Assert.assertTrue(e.hasTags());
        Assert.assertEquals(1, e.tags.length);

        e = CachedEosKeyResolver.parse("inc://test.key:tag1@localhost:tag2:tagN");
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertTrue(e.hasServer());
        Assert.assertTrue(e.hasTags());
        Assert.assertEquals(3, e.tags.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveMultipleServers()
    {
        CachedEosKeyResolver.parse("log://test@localhost@remote");
    }

    @Test
    public void testRecombine()
    {
        EosKey k;
        EosKey[] rc;
        k = new EosKey(EosKey.Schema.log, "test", null);
        Assert.assertEquals(1, CachedEosKeyResolver.recombination(k).length);
        Assert.assertSame(k, CachedEosKeyResolver.recombination(k)[0]);

        k  = new EosKey(EosKey.Schema.log, "test", null, "a", "b");
        rc = CachedEosKeyResolver.recombination(k);
        Assert.assertEquals(1 + 1 + 2, rc.length);
        Assert.assertEquals("log://test:a:b", rc[0].toString());
        Assert.assertEquals("log://test:a", rc[1].toString());
        Assert.assertEquals("log://test:b", rc[2].toString());
        Assert.assertEquals("log://test", rc[3].toString());

        k  = new EosKey(EosKey.Schema.log, "test", null, "c", "b", "a");
        rc = CachedEosKeyResolver.recombination(k);
        Assert.assertEquals(1 + 1 + 6, rc.length);
        Assert.assertEquals("log://test:a:b:c", rc[0].toString());
        Assert.assertEquals("log://test:a:b", rc[1].toString());
        Assert.assertEquals("log://test:a:c", rc[2].toString());
        Assert.assertEquals("log://test:b:c", rc[3].toString());
        Assert.assertEquals("log://test:a", rc[4].toString());
        Assert.assertEquals("log://test:b", rc[5].toString());
        Assert.assertEquals("log://test:c", rc[6].toString());
        Assert.assertEquals("log://test", rc[7].toString());

        k = new EosKey(EosKey.Schema.log, "test", null, "c", "z", "j", "2", "b", "a");
        rc = CachedEosKeyResolver.recombination(k);
        Assert.assertEquals(878, rc.length);
    }
}
