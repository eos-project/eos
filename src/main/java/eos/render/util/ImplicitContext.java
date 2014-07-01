package eos.render.util;

import eos.render.Context;

import java.io.PrintStream;

public class ImplicitContext implements Context
{
    private final PrintStream stream;

    public ImplicitContext() {
        this(System.out);
    }

    public ImplicitContext(PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public Context add(CharSequence cs) {
        this.stream.print(cs);
        return this;
    }

    @Override
    public Context nl() {
        this.stream.print("\n");
        return this;
    }

    @Override
    public String toString() {
        return "";
    }
}
