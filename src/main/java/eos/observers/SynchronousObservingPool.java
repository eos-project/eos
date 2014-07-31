package eos.observers;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronous observing pool
 */
public class SynchronousObservingPool implements ObservingPool
{
    /**
     * Observers list
     */
    final List<Observer> observers;

    /**
     * Constructor
     */
    public SynchronousObservingPool() {
        this.observers   = new ArrayList<>(10);
    }

    @Override
    public void register(Observer observer)
    {
        if (observer == null) {
            throw new NullPointerException("observer");
        }

        unregister(observer);
        observers.add(observer);
    }

    @Override
    public void unregister(Observer observer)
    {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    @Override
    public void report(final ObservingEvent event)
    {
        for (Observer o : observers) {
            o.report(event);
        }
    }
}
