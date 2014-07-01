package eos.observers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneralObservingPool
{
    final List<Observer> observers;
    final ExecutorService pool;

    public GeneralObservingPool() {
        this.observers   = new ArrayList<>(50);

        pool = Executors.newFixedThreadPool(50);
    }

    public void register(Observer observer)
    {
        if (observer == null) {
            throw new NullPointerException("observer");
        }

        unregister(observer);
        observers.add(observer);
    }

    public void unregister(Observer observer)
    {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    public void send(final ObservingEvent event)
    {
        pool.submit(() -> {
            for (Observer o : observers) {
                o.report(event);
            }
        });
    }

}
