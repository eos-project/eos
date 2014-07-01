package eos.metrics;

import eos.render.Renderable;
import eos.type.LongIncrement;
import eos.type.EosKey;

import java.util.List;

public class KeyDispatcherLongIncrement implements LongIncrement
{
    final LongIncrement main;
    final LongIncrement[] others;

    public KeyDispatcherLongIncrement(LongIncrement main, List<LongIncrement> others)
    {
        this.main = main;
        this.others = others.toArray(new LongIncrement[others.size()]);
    }

    @Override
    public EosKey getKey() {
        return main.getKey();
    }

    @Override
    public Renderable export() {
        return main.export();
    }

    @Override
    public void inc() {
        add(1);
    }

    @Override
    public void add(long value) {
        main.add(value);
        for (LongIncrement l : others) {
            l.add(value);
        }
    }

    @Override
    public long getValue() {
        return main.getValue();
    }
}
