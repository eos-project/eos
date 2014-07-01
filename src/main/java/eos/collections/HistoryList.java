package eos.collections;

import java.util.List;

public interface HistoryList <T>
{
    /**
     * Adds new entry to the list
     *
     * @param value value to add
     */
    void add(T value);
    /**
     * @return Last added value
     */
    public T getValue();
    /**
     * @return List of values in the history
     */
    public List<T> asList();
    /**
     * @return Current size
     */
    int size();
    /**
     * @return Configured depth
     */
    int depth();
}
