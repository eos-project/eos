package eos.render;

public abstract class AbstractContext implements Context
{
    private final Context innerContext;

    protected AbstractContext(Context innerContext) {
        this.innerContext = innerContext;
    }

    @Override
    public Context add(CharSequence cs) {
        innerContext.add(cs);
        return this;
    }

    @Override
    public Context nl() {
        innerContext.nl();
        return this;
    }

    @Override
    public String toString() {
        return innerContext.toString();
    }
}
