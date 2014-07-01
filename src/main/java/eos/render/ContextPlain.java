package eos.render;

import eos.render.util.BufferedContext;

public class ContextPlain extends AbstractContext
{
    public ContextPlain()
    {
        this(new BufferedContext());
    }

    public ContextPlain(Context innerContext) {
        super(innerContext);
    }
}
