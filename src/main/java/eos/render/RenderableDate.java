package eos.render;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RenderableDate implements Renderable
{
    final static TimeZone tz = TimeZone.getTimeZone("UTC");
    final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static
    {
        df.setTimeZone(tz);
    }

    final Date date;

    public RenderableDate(Date date) {
        this.date = date;
    }

    @Override
    public void render(Context ctx) {
        if (ctx instanceof ContextJson) {
            ctx.add("\"").add(df.format(date)).add("\"");
        } else {
            ctx.add(date.toString());
        }
    }
}
