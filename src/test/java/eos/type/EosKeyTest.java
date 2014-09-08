package eos.type;

import org.junit.Assert;
import org.junit.Test;

public class EosKeyTest
{
    @Test
    public void testAlphabeticalKeys()
    {
        EosKey k;

        k = new EosKey("a1", EosKey.Schema.log, "test", "a", "b", "c");
        Assert.assertEquals("a1+log://test:a:b:c", k.toString());

        k = new EosKey("bcx", EosKey.Schema.log, "test", "b", "a", "c");
        Assert.assertEquals("bcx+log://test:a:b:c", k.toString());

        k = new EosKey("f", EosKey.Schema.log, "test", "b", "z", "c", "a");
        Assert.assertEquals("f+log://test:a:b:c:z", k.toString());

        k = new EosKey("z", EosKey.Schema.log, "test", "b", "z", "c", "a", "k", "e", "w", "h");
        Assert.assertEquals("z+log://test:a:b:c:e:h:k:w:z", k.toString());
    }
}
