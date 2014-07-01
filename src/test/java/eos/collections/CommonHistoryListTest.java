package eos.collections;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class CommonHistoryListTest
{
    @Test
    public void testConstructWrongDepth()
    {
        try {
            new CommonHistoryList<String>(-2);
            Assert.fail();
        } catch (IllegalArgumentException e){
            Assert.assertTrue(true);
        }
        try {
            new CommonHistoryList<String>(0);
            Assert.fail();
        } catch (IllegalArgumentException e){
            Assert.assertTrue(true);
        }
        try {
            new CommonHistoryList<>("", -2);
            Assert.fail();
        } catch (IllegalArgumentException e){
            Assert.assertTrue(true);
        }
        try {
            new CommonHistoryList<>("", 0);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testDepthSet()
    {
        Assert.assertEquals(15, new CommonHistoryList<>("", 15).depth());
        Assert.assertEquals(2, new CommonHistoryList<Integer>(2).depth());
    }

    @Test
    public void testInitialConstructor()
    {
        Assert.assertEquals(0, new CommonHistoryList<String>(10).size());
        Assert.assertEquals(1, new CommonHistoryList<>("Hello",10).size());
        Assert.assertEquals(1, new CommonHistoryList<>("",10).size());
        Assert.assertEquals(501L, (long) new CommonHistoryList<>(501, 10).getValue());
    }

    @Test
    public void testHistory()
    {
        // Creating list with 3 entries
        CommonHistoryList<Integer> x = new CommonHistoryList<>(3);

        // Adding two entries
        x.add(555);
        x.add(777);

        // Checking
        Assert.assertEquals(2, x.size());
        Assert.assertEquals(777, (int) x.getValue());
        Assert.assertEquals("777 + 555", StringUtils.join(x.asList(), " + "));

        // Adding 10 more entries
        // This must overflow list
        for (int i = 0; i < 10; i++) {
            x.add(i);
        }

        Assert.assertEquals(3, x.size());
        Assert.assertEquals(9, (int) x.getValue());
        Assert.assertEquals(12, x.inserted);
        Assert.assertEquals("9 :: 8 :: 7", StringUtils.join(x.asList(), " :: "));

        // Minimal
        // Creating list with 1 entry
        x = new CommonHistoryList<>(1);
        for (int i = 0; i < 10; i++) {
            x.add(i);
        }
        Assert.assertEquals(1, x.size());
        Assert.assertEquals(9, (int) x.getValue());
        Assert.assertEquals(10, x.inserted);
        Assert.assertEquals("9", StringUtils.join(x.asList(), " :: "));
    }
}
