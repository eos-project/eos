package eos.render;

public class RenderableObject implements Renderable
{
    final Object value;

    public RenderableObject(Object value) {
        this.value = value;
    }

    @Override
    public void render(Context ctx)
    {
        if (value instanceof Renderable) {
            ((Renderable) value).render(ctx);
        } else if (value == null) {
            if (ctx instanceof ContextJson) {
                ctx.add("null");
            } else {
                ctx.add("<null>");
            }
        } else {
            if (ctx instanceof ContextJson) {
                if (value instanceof Number) {
                    ctx.add(value.toString());
                } else {
                    ((ContextJson) ctx).addEscapedString(value.toString());
                }
            } else {
                ctx.add(value.toString());
            }
        }
    }
}
