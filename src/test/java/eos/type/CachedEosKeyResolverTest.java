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
        Assert.assertFalse(e.hasTags());

        e = CachedEosKeyResolver.parse("inc://test.key:tag1");
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertTrue(e.hasTags());
        Assert.assertEquals(1, e.tags.length);
        Assert.assertTrue(e.hasTag("tag1"));
        Assert.assertFalse(e.hasTag("tag2"));

        e = CachedEosKeyResolver.parse("inc://test.key:tag1:tag2:tagN");
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals("test.key", e.key);
        Assert.assertTrue(e.hasTags());
        Assert.assertEquals(3, e.tags.length);
    }

    @Test
    public void testRecombine()
    {
        EosKey k;
        EosKey[] rc;
        k = new EosKey(EosKey.Schema.log, "test");
        CachedEosKeyResolver cekr = new CachedEosKeyResolver((byte) 10, 10, 10);
        Assert.assertEquals(1, cekr.recombination(k).length);
        Assert.assertSame(k, cekr.recombination(k)[0]);

        k  = new EosKey(EosKey.Schema.log, "test", "a", "b");
        rc = cekr.recombination(k);
        Assert.assertEquals(1 + 1 + 2, rc.length);
        Assert.assertEquals("log://test:a:b", rc[0].toString());
        Assert.assertEquals("log://test:a", rc[1].toString());
        Assert.assertEquals("log://test:b", rc[2].toString());
        Assert.assertEquals("log://test", rc[3].toString());

        k  = new EosKey(EosKey.Schema.log, "test", "c", "b", "a");
        rc = cekr.recombination(k);
        Assert.assertEquals(1 + 1 + 6, rc.length);
        Assert.assertEquals("log://test:a:b:c", rc[0].toString());
        Assert.assertEquals("log://test:a:b", rc[1].toString());
        Assert.assertEquals("log://test:a:c", rc[2].toString());
        Assert.assertEquals("log://test:b:c", rc[3].toString());
        Assert.assertEquals("log://test:a", rc[4].toString());
        Assert.assertEquals("log://test:b", rc[5].toString());
        Assert.assertEquals("log://test:c", rc[6].toString());
        Assert.assertEquals("log://test", rc[7].toString());

        k = new EosKey(EosKey.Schema.log, "test", "c", "z", "j", "2", "b", "a");
        rc = cekr.recombination(k);
        Assert.assertEquals(878, rc.length);
    }

    @Test
    public void testRecombineLimit()
    {
        CachedEosKeyResolver cekr = new CachedEosKeyResolver((byte) 2, 10, 10);
        EosKey k  = new EosKey(EosKey.Schema.log, "test", "c", "b", "a");
        EosKey[] rc = cekr.recombination(k);

        Assert.assertSame(1, rc.length);
        Assert.assertSame(k, rc[0]);
    }
}
