package eos.logging;

import eos.render.Renderable;
import eos.type.Logger;
import eos.type.EosKey;

import java.util.Date;
import java.util.List;

public class KeyDispatcherLogger implements Logger
{
    final Logger main;
    final Logger[] others;

    public KeyDispatcherLogger(Logger main, List<Logger> others)
    {
        this.main = main;
        this.others = others.toArray(new Logger[others.size()]);
    }

    @Override
    public void log(String data) {
        log(new Date(), data);
    }

    @Override
    public void log(Date date, String data)
    {
        this.main.log(date, data);
        for (Logger other : others) {
            other.log(date, data);
        }
    }

    @Override
    public EosKey getKey() {
        return main.getKey();
    }

    @Override
    public Renderable export() {
        return main.export();
    }
}
