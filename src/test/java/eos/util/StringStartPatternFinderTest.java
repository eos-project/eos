package eos.util;

import org.junit.Assert;
import org.junit.Test;

public class StringStartPatternFinderTest
{
    @Test
    public void check()
    {
        StringStartPatternFinder f = new StringStartPatternFinder();

        Assert.assertEquals("ab", f.findPattern(new String[]{"abcd", "abhd", "abc"}));
        Assert.assertEquals("12", f.findPattern(new String[]{"12345", "12"}));
        Assert.assertEquals("fasdf", f.findPattern(new String[]{"fasdf"}));
        Assert.assertEquals("", f.findPattern(new String[0]));
        Assert.assertEquals("", f.findPattern(null));
    }
}
