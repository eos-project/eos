package eos.server;

import eos.EosRegistry;
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
    final EosKeyCombinator combinator;

    public CommonEosRegistry(int capacity, EosKeyCombinator combinator)
    {
        this.entriesMap = new HashMap<>(capacity);
        this.combinator = combinator;
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
                    switch (key.getSchema()) {
                        case log:
                            answer = new CommonLogger(key, 50);
                            break;
                        case inc:
                            answer = new CommonIncrement(key);
                            break;
                        default:
                            throw new RuntimeException("Unknown schema " + key.getSchema());
                    }
                    entriesMap.put(key, answer);
                }
            }
        }

        return answer;
    }

    @Override
    public void report(ObservingEvent event) {
        if (event instanceof LoggersObserver.Event) {
            LoggersObserver.Event loggerEvent = (LoggersObserver.Event) event;
            EosKey origin = loggerEvent.getKey();
            for (EosKey key : combinator.getCombinations(origin)) {
                ((Logger) take(key)).log(loggerEvent.getLine());
            }
        } else if (event instanceof IncrementObserver.Event) {
            IncrementObserver.Event incEvent = (IncrementObserver.Event) event;
            EosKey origin = incEvent.getKey();
            for (EosKey key : combinator.getCombinations(origin)) {
                ((LongIncrement) take(key)).add(incEvent.getDelta());
            }
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
}
