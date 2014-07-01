package eos.render;

import java.util.LinkedHashMap;

public class RenderableMap extends LinkedHashMap<String, Renderable> implements Renderable
{
    public RenderableMap()
    {
        super();
    }

    public RenderableMap(String key1, Renderable value1, String key2, Renderable value2)
    {
        this();
        put(key1, value1);
        put(key2, value2);
    }

    public Renderable put(String key, Object value) {
        if (value instanceof Renderable) {
            return put(key, (Renderable) value);
        } else {
            return put(key, new RenderableObject(value));
        }
    }

    @Override
    public void render(Context ctx) {
        if (ctx instanceof ContextJson) {
            renderJson((ContextJson) ctx);
        } else {
            ctx.add("not implemented");
        }
    }

    void renderJson(ContextJson ctx)
    {
        int limit = this.size() - 1;
        ctx.add("{");
        if (limit > 3) {
            ctx.currentLevel++;
            ctx.nl();
        }
        int i = 0;
        for (String key : this.keySet()){
            if (limit > 3) {
                ctx.indent();
            }
            ctx.addEscapedString(key);
            ctx.add(": ");
            get(key).render(ctx);
            if (i < limit) {
                ctx.add(",");
            }
            if (limit > 3) ctx.nl();
            i++;
        }
        if (limit > 3) {
            ctx.currentLevel--;
            ctx.indent();
        }
        ctx.add("}");
    }
}
