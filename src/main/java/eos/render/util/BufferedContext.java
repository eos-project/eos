package eos.render.util;

import eos.render.Context;

public class BufferedContext implements Context
{
    final StringBuilder stringBuilder = new StringBuilder();

    @Override
    public BufferedContext add(CharSequence cs)
    {
        stringBuilder.append(cs);
        return this;
    }

    @Override
    public BufferedContext nl()
    {
        stringBuilder.append("\n");
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
