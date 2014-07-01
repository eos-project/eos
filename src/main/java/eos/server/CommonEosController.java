package eos.server;

import eos.EosController;
import eos.EosRegistry;
import eos.access.AccessTokenRepository;
import eos.filters.StarPatternFilter;
import eos.observers.ObservingEvent;
import eos.observers.ObservingPool;
import eos.type.EosEntry;
import eos.type.EosKey;

import java.util.List;
import java.util.stream.Collectors;

public class CommonEosController implements EosController
{
    final EosRegistry metricRegistry;
    final AccessTokenRepository accessTokenRepository;
    final ObservingPool pool;
    final boolean allowAllRead;

    public CommonEosController(EosRegistry metricRegistry,
                               ObservingPool observer,
                               AccessTokenRepository accessTokenRepository,
                               boolean allowAllRead
    ) {
        this.pool = observer;
        this.metricRegistry = metricRegistry;
        this.accessTokenRepository = accessTokenRepository;
        this.allowAllRead = allowAllRead;
    }

    @Override
    public AccessTokenRepository getTokenRepository() {
        return accessTokenRepository;
    }

    @Override
    public EosEntry getMetricRead(String token, EosKey key) throws WrongTokenException, EntryNotFoundException {
        if (!accessTokenRepository.isAllowedRead(token, key)) {
            throw new WrongTokenException(token);
        }

        if (!metricRegistry.contains(key)) {
            throw new EntryNotFoundException(key.toString());
        }

        return metricRegistry.take(key);
    }

    @Override
    public void sendEvent(String token, ObservingEvent event) throws WrongTokenException {
        if (!accessTokenRepository.isAllowedWrite(token, event.getKey())) {
            throw new WrongTokenException(token);
        }

        pool.report(event);
    }

    @Override
    public List<String> findMetrics(String pattern) {
        return metricRegistry
                .getKeys(new StarPatternFilter(pattern))
                .stream()
                .map(EosKey::toString)
                .sorted()
                .collect(Collectors.toList());
    }

}
