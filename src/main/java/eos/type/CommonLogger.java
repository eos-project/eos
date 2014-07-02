package eos.type;

import eos.collections.CommonHistoryList;
import eos.collections.HistoryList;
import eos.render.*;

import java.util.Date;

public class CommonLogger implements Logger, EosEntry
{
    final HistoryList<History> historyList;
    final EosKey key;

    public CommonLogger(EosKey key, int depth)
    {
        this.historyList = new CommonHistoryList<History>(
            new History(new Date(), "CommonLogger created"),
            depth
        );
        this.key = key;
    }

    @Override
    public void log(String data) {
        log(new Date(), data);
    }

    public void log(Date time, String data)
    {
        if (time == null) {
            time = new Date();
        }
        if (data == null) {
            data = "";
        }

        historyList.add(new History(time, data));
    }

    @Override
    public EosKey getKey() {
        return key;
    }

    @Override
    public int hashCode()
    {
        return key.hashCode();
    }

    @Override
    public Renderable export() {
        RenderableList answer = new RenderableList();
        for (History h : historyList.asList()) {
            answer.add(h);
        }
        return answer;
    }

    static class History implements Renderable
    {
        final Date time;
        final String data;

        History(Date time, String data) {
            this.time = time;
            this.data = data;
        }

        @Override
        public void render(Context ctx) {
            RenderableList x = new RenderableList();
            x.add(new RenderableDate(time));
            x.add(new RenderableObject(data));
            x.render(ctx);
        }
    }
}
