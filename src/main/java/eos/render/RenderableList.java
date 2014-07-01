package eos.render;

import java.util.ArrayList;
import java.util.List;

public class RenderableList extends ArrayList<Renderable> implements Renderable
{
    public RenderableList() {}
    public RenderableList(List source)
    {
        for (Object s : source) {
            this.add(new RenderableObject(s));
        }
    }

    @Override
    public void render(Context ctx)
    {
       if (ctx instanceof ContextJson) {
           renderJson((ContextJson) ctx);
       } else {
           for (Renderable r : this) {
               r.render(ctx);
               ctx.nl();
           }
       }
    }

    void renderJson(ContextJson ctx)
    {
        int limit = this.size() - 1;
        ctx.add("[");
        if (limit > 3) {
            ctx.currentLevel++;
            ctx.nl();
        }
        for (int i=0; i <= limit ; i++ ){
            if (limit > 3) {
                ctx.indent();
            }
            get(i).render(ctx);
            if (i < limit) {
                ctx.add(",");
            }
            if (limit > 3) ctx.nl();
        }
        if (limit > 3) {
            ctx.currentLevel--;
            ctx.indent();
        }
        ctx.add("]");
    }
}
