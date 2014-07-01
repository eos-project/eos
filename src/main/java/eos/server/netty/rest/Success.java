package eos.server.netty.rest;

import eos.render.ContextJson;
import eos.render.Renderable;

public class Success implements RestResponse
{
    ContextJson ctx = new ContextJson();

    public Success(Renderable r)
    {
       r.render(ctx);
    }

    @Override
    public int getCode() {
        return 200;
    }

    @Override
    public String toString() {
        return ctx.toString();
    }

    @Override
    public String getContentType() {
        return "application/json; charset=UTF-8";
    }
}
