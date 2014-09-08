package eos.type;

import org.junit.Assert;
import org.junit.Test;

public class CachedEosKeyResolverTest
{
    @Test
    public void testResolveSuccess()
    {
        EosKey e = CachedEosKeyResolver.parse("foo+log://test.key");
        Assert.assertEquals("foo", e.getRealm());
        Assert.assertEquals(EosKey.Schema.log, e.getSchema());
        Assert.assertEquals(1, e.tags.length);
        Assert.assertTrue(e.hasTag("test.key"));
        Assert.assertFalse(e.hasTag("test"));

        e = CachedEosKeyResolver.parse("bar+inc://test.key:tag1");
        Assert.assertEquals("bar", e.getRealm());
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals(2, e.tags.length);
        Assert.assertTrue(e.hasTag("test.key"));
        Assert.assertTrue(e.hasTag("tag1"));
        Assert.assertFalse(e.hasTag("test"));

        e = CachedEosKeyResolver.parse("z_0-a+inc://test.key:tag1:tag2:tagN");
        Assert.assertEquals("z_0-a", e.getRealm());
        Assert.assertEquals(EosKey.Schema.inc, e.getSchema());
        Assert.assertEquals(4, e.tags.length);
        Assert.assertTrue(e.hasTag("test.key"));
        Assert.assertTrue(e.hasTag("tag2"));
        Assert.assertFalse(e.hasTag("test"));
    }

    @Test
    public void testRecombine()
    {
        EosKey k;
        EosKey[] rc;
        k = new EosKey("x", EosKey.Schema.log, "test");
        CachedEosKeyResolver cekr = new CachedEosKeyResolver((byte) 10, 10, 10);
        Assert.assertEquals(1, cekr.recombination(k).length);
        Assert.assertSame(k, cekr.recombination(k)[0]);

        k  = new EosKey("x", EosKey.Schema.log, "test", "a", "b");
        rc = cekr.recombination(k);
        Assert.assertEquals(7, rc.length);

        k = new EosKey("a",EosKey.Schema.log, "test", "c", "z", "j", "2", "b", "a");
        rc = cekr.recombination(k);
        Assert.assertEquals(6140, rc.length);
    }

    @Test
    public void testRecombineLimit()
    {
        CachedEosKeyResolver cekr = new CachedEosKeyResolver((byte) 2, 10, 10);
        EosKey k  = new EosKey("o", EosKey.Schema.log, "test", "c", "b", "a");
        EosKey[] rc = cekr.recombination(k);

        Assert.assertSame(1, rc.length);
        Assert.assertSame(k, rc[0]);
    }
}
