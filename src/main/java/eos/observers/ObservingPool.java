package eos.observers;

public interface ObservingPool extends Observer
{
    /**
     * Registers new observer
     * @param observer Observer to register
     */
    public void register(Observer observer);

    /**
     * Unregister observer
     *
     * @param observer Observer to unregister
     */
    public void unregister(Observer observer);
}
