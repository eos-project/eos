package eos.metrics;

import eos.collections.TimeSampledHistoryList;
import eos.render.RenderableList;
import eos.render.RenderableMap;
import eos.render.RenderableObject;
import eos.type.LongIncrement;
import eos.type.EosEntry;
import eos.type.EosKey;

import java.util.List;

public class CommonIncrement implements TimeSampledHistoryList.Provider<CommonIncrement.UsagePair>, EosEntry, LongIncrement
{
    final TimeSampledHistoryList<UsagePair> historyList;
    final EosKey name;
    final UsagePair total;

    class UsagePair
    {
        long value;
        long usages;

        final Object lock = new Object();

        UsagePair(long value) {
            this.value  = value;
            this.usages = 0;
        }

        void add(long value)
        {
            synchronized (lock) {
                this.value += value;
                this.usages ++;
            }
        }
    }

    public CommonIncrement(EosKey name)
    {
        this.name  = name;
        this.total = new UsagePair(0L);
        historyList = new TimeSampledHistoryList<>(
            TimeSampledHistoryList.sampleRateMinute,
            15,
            this
        );
    }

    @Override
    public UsagePair provide() {
        return new UsagePair(0L);
    }

    @Override
    public EosKey getKey() {
        return name;
    }

    public void inc() {
        add(1L);
    }

    public void add(long value) {
        historyList.getValue().add(value);
        total.add(value);
    }

    public long getValue()
    {
        return historyList.getValue().value;
    }

    @Override
    public RenderableMap export() {
        RenderableMap map = new RenderableMap();
        map.put("name", new RenderableObject(getKey()));

        RenderableMap counter = new RenderableMap();
        synchronized (total.lock) {
            counter.put("value", new RenderableObject(total.value));
            counter.put("changes", new RenderableObject(total.usages));
        }
        map.put("counter", counter);

        List<UsagePair> history = historyList.asList();
        RenderableList samples = new RenderableList();
        for (UsagePair up : history) {
            samples.add(
                new RenderableMap(
                    "value",
                    new RenderableObject(up.value),
                    "usages",
                    new RenderableObject(up.usages)
                )
            );
        }
        map.put("samples", samples);

        RenderableMap tmp;
        // Current sample
        tmp = new RenderableMap();
        tmp.put("value", history.get(0).value);
        tmp.put("usages", history.get(0).usages);
        tmp.put("rps", (float) history.get(0).usages / 60f);
        map.put("sampleCurrent", tmp);
        // Previous sample
        tmp = new RenderableMap();
        tmp.put("value", history.get(1).value);
        tmp.put("usages", history.get(1).usages);
        tmp.put("rps", (float) history.get(1).usages / 60f);
        map.put("samplePrevious", tmp);
        // 5 minute avg
        tmp = new RenderableMap();
        long val = 0, usg = 0;
        for (int i=0; i < 5; i++) {
            val += history.get(i).value;
            usg += history.get(i).usages;
        }
        tmp.put("value", val);
        tmp.put("usages", usg);
        tmp.put("avg", val / 5);
        tmp.put("rps", usg / 60f / 5f);
        map.put("sample5min", tmp);
        // 5 minute avg
        tmp = new RenderableMap();
        val = 0; usg = 0;
        for (int i=0; i < 10; i++) {
            val += history.get(i).value;
            usg += history.get(i).usages;
        }
        tmp.put("value", val);
        tmp.put("usages", usg);
        tmp.put("avg", val / 10);
        tmp.put("rps", usg / 60f / 10f);
        map.put("sample10min", tmp);
        // 15 minute avg
        tmp = new RenderableMap();
        val = 0; usg = 0;
        for (int i=0; i < 15; i++) {
            val += history.get(i).value;
            usg += history.get(i).usages;
        }
        tmp.put("value", val);
        tmp.put("usages", usg);
        tmp.put("avg", val / 15);
        tmp.put("rps", usg / 60f / 15f);
        map.put("sample15min", tmp);

        return map;
    }
}
