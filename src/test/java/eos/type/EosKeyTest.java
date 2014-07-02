package eos.type;

import org.junit.Assert;
import org.junit.Test;

public class EosKeyTest
{
    @Test
    public void testAlphabeticalKeys()
    {
        EosKey k;

        k = new EosKey(EosKey.Schema.log, "test", null, "a", "b", "c");
        Assert.assertEquals("log://test:a:b:c", k.toString());

        k = new EosKey(EosKey.Schema.log, "test", null, "b", "a", "c");
        Assert.assertEquals("log://test:a:b:c", k.toString());

        k = new EosKey(EosKey.Schema.log, "test", null, "b", "z", "c", "a");
        Assert.assertEquals("log://test:a:b:c:z", k.toString());

        k = new EosKey(EosKey.Schema.log, "test", "localhost", "b", "z", "c", "a", "k", "e", "w", "h");
        Assert.assertEquals("log://test@localhost:a:b:c:e:h:k:w:z", k.toString());
    }
}
