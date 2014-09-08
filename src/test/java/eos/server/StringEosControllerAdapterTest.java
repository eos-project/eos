package eos.server;

import eos.EosController;
import eos.observers.ObservingEvent;
import eos.realm.RealmDescriptor;
import eos.type.CachedEosKeyResolver;
import eos.type.EosEntry;
import eos.type.EosKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class StringEosControllerAdapterTest
{
    @Test
    public void testFormat1() throws Exception
    {
        StringEosControllerAdapter a = new StringEosControllerAdapter(
            new EosController() {
                @Override
                public EosEntry getMetricRead(EosKey name) throws WrongTokenException, EntryNotFoundException {
                    return null;
                }

                @Override
                public List<String> findMetrics(String pattern) {
                    return null;
                }

                @Override
                public void sendEvent(ObservingEvent event) throws WrongTokenException {

                }
            },
            new RealmDescriptor() {
                @Override
                public boolean allowed(EosKey key, String nonce, String payload, String signature) {
                    Assert.assertEquals("nonce1", nonce);
                    Assert.assertEquals("x1+log://test:tag", key.toString());
                    Assert.assertEquals("hello", signature);
                    return true;
                }

                @Override
                public boolean allowed(String realm, String nonce, String payload, String signature) {
                    return false;
                }
            },
            new CachedEosKeyResolver((byte) 10, 1, 1)
        );

        a.process(new String[] {"nonce1", "hello", "x1+log://test:tag"});
    }

    @Test
    public void testFormat2() throws Exception
    {
        StringEosControllerAdapter a = new StringEosControllerAdapter(
                new EosController() {
                    @Override
                    public EosEntry getMetricRead(EosKey name) throws WrongTokenException, EntryNotFoundException {
                        return null;
                    }

                    @Override
                    public List<String> findMetrics(String pattern) {
                        return null;
                    }

                    @Override
                    public void sendEvent(ObservingEvent event) throws WrongTokenException {

                    }
                },
                new RealmDescriptor() {
                    @Override
                    public boolean allowed(EosKey key, String nonce, String payload, String signature) {
                        Assert.assertEquals("foobar", nonce);
                        Assert.assertEquals("zzz+log://test:tag", key.toString());
                        Assert.assertEquals("hi", signature);
                        return true;
                    }
                    @Override
                    public boolean allowed(String realm, String nonce, String payload, String signature) {
                        return false;
                    }
                },
                new CachedEosKeyResolver((byte) 10, 1, 1)
        );

        a.process(new String[] {"foobar", "zzz+hi", "log://test:tag"});
    }
}
