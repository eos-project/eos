package eos.server;

import eos.EosController;
import eos.EosRegistry;
import eos.filters.StarPatternFilter;
import eos.observers.ObservingEvent;
import eos.observers.ObservingPool;
import eos.type.EosEntry;
import eos.type.EosKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommonEosController implements EosController
{
    final EosRegistry metricRegistry;
    final ObservingPool pool;
    final boolean allowAllRead;

    public CommonEosController(EosRegistry metricRegistry,
                               ObservingPool observer,
                               boolean allowAllRead
    ) {
        this.pool = observer;
        this.metricRegistry = metricRegistry;
        this.allowAllRead = allowAllRead;
    }

    @Override
    public EosEntry getMetricRead(EosKey key) throws WrongTokenException, EntryNotFoundException {

        if (!metricRegistry.contains(key)) {
            throw new EntryNotFoundException(key.toString());
        }

        return metricRegistry.take(key);
    }

    @Override
    public void sendEvent(ObservingEvent event) throws WrongTokenException {
        pool.report(event);
    }

    @Override
    public List<String> findMetrics(String pattern) {
        List<String> answer = new ArrayList<>();
        for (EosKey key : metricRegistry.getKeys(new StarPatternFilter(pattern))) {
            answer.add(key.toString());
        }
        Collections.sort(answer);
        return answer;
    }

}
