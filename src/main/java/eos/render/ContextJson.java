package eos.render;

import eos.render.util.BufferedContext;
import org.apache.commons.lang.StringUtils;

public class ContextJson extends AbstractContext
{
    public int currentLevel = 0;

    public ContextJson()
    {
        this(new BufferedContext());
    }

    public ContextJson(Context innerContext) {
        super(innerContext);
    }

    public void indent()
    {
        this.add(StringUtils.repeat("    ", currentLevel));
    }

    public void addEscapedString(String str)
    {
        this.add("\"").add(str.replace("\"", "\\\"")).add("\"");
    }
}
