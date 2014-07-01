package eos.server;

import eos.EosRegistry;
import eos.logging.CommonLogger;
import eos.logging.KeyDispatcherLogger;
import eos.metrics.CommonIncrement;
import eos.metrics.KeyDispatcherLongIncrement;
import eos.observers.IncrementObserver;
import eos.observers.LoggersObserver;
import eos.observers.ObservingEvent;
import eos.type.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommonEosRegistry implements EosRegistry
{
    final Object lock = new Object();
    final Map<EosKey, EosEntry> entriesMap;

    public CommonEosRegistry(int capacity)
    {
        entriesMap = new HashMap<>(capacity);
    }

    @Override
    public boolean contains(EosKey key) {
        return entriesMap.containsKey(key);
    }

    @Override
    public EosEntry take(EosKey key) {
        EosEntry answer = entriesMap.get(key);
        if (answer == null) {
            synchronized (lock) {
                answer = entriesMap.get(key);
                if (answer == null) {
                    answer = init(key);
                }
            }
        }

        return answer;
    }

    @Override
    public void report(ObservingEvent event) {
        if (event instanceof LoggersObserver.Event) {
            ((Logger) take(event.getKey())).log(((LoggersObserver.Event) event).getLine());
        } else if (event instanceof IncrementObserver.Event) {
            ((LongIncrement) take(event.getKey())).add(((IncrementObserver.Event) event).getDelta());
        }
    }

    @Override
    public List<EosKey> getKeys(KeyFilter filter) {
        List<EosKey> answer = new ArrayList<>();
        Map<EosKey, EosEntry> copy = this.entriesMap;
        answer.addAll(
            copy.keySet()
                .stream()
                .filter(filter::matches)
                .collect(Collectors.toList())
        );

        return answer;
    }

    private EosEntry init(EosKey key)
    {
        if (key.schemaEquals(EosKey.Schema.log)) {
            return initLoggers(key);
        } else if (key.schemaEquals(EosKey.Schema.inc)) {
            return initLongIncrements(key);
        } else {
            throw new RuntimeException("not implemented");
        }
    }

    private LongIncrement initLongIncrements(EosKey key)
    {
        LongIncrement mainInc = new CommonIncrement(key);

        if (key.hasTags() || key.hasServer()) {
            // Dispatcher needed
            List<LongIncrement> other = new ArrayList<>();
            if (key.hasTags()) {
                other.add(new CommonIncrement(key.withoutTags()));
            }
            if (key.hasServer()) {
                other.add(new CommonIncrement(key.withoutServer()));
            }
            if (key.hasServer() && key.hasTags()) {
                other.add(new CommonIncrement(key.withoutServerAndTag()));
            }

            // Inserting
            for (int i=0; i < other.size(); i++) {
                LongIncrement l = other.get(i);
                if (entriesMap.containsKey(l.getKey())) {
                    other.set(i, (LongIncrement) entriesMap.get(l.getKey()));
                } else {
                    entriesMap.put(l.getKey(), l);
                }
            }
            // Envelope
            mainInc = new KeyDispatcherLongIncrement(mainInc, other);
        }
        entriesMap.put(mainInc.getKey(), mainInc);
        return mainInc;
    }

    private Logger initLoggers(EosKey key)
    {
        Logger mainLogger = new CommonLogger(key, 50);

        if (key.hasTags() || key.hasServer()) {
            // Dispatcher needed
            List<Logger> other = new ArrayList<>();
            if (key.hasTags()) {
                other.add(new CommonLogger(key.withoutTags(), 50));
            }
            if (key.hasServer()) {
                other.add(new CommonLogger(key.withoutServer(), 50));
            }
            if (key.hasServer() && key.hasTags()) {
                other.add(new CommonLogger(key.withoutServerAndTag(), 50));
            }

            // Inserting
            for (int i=0; i < other.size(); i++) {
                Logger l = other.get(i);
                if (entriesMap.containsKey(l.getKey())) {
                    other.set(i, (Logger) entriesMap.get(l.getKey()));
                } else {
                    entriesMap.put(l.getKey(), l);
                }
            }
            // Envelope
            mainLogger = new KeyDispatcherLogger(mainLogger, other);
        }
        entriesMap.put(mainLogger.getKey(), mainLogger);
        return mainLogger;
    }
}
