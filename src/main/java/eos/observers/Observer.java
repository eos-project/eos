package eos.observers;

public interface Observer
{
    /**
     * Reports new observing event
     *
     * @param event New event
     */
    void report(ObservingEvent event);
}
