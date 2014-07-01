package eos.collections;

import java.util.ArrayList;
import java.util.List;

public class CommonHistoryList<T> implements HistoryList<T> {

    /**
     * Configured maximum size of the list
     */
    final int depth;

    /**
     * Amount of inserted during lifetime values
     */
    long inserted;

    /**
     * Current list size
     */
    int size = 0;

    /**
     * Lock
     */
    final Object lock = new Object();

    /**
     * Top entry node, pointing to oldest added entry
     */
    Node<T> newest;

    /**
     * Last entry node, pointing to first added entry
     */
    Node<T> oldest;

    /**
     * Utility class
     *
     * @param <T>
     */
    static class Node <T>
    {
        T value;
        Node<T> older;
        Node<T> newer;

        Node(T value, Node<T> older) {
            this.value = value;
            this.older = older;
        }

        public String toString()
        {
            return value.toString();
        }
    }

    /**
     * Builds new list with initial value
     *
     * @param value Initial value
     * @param depth Maximum list size
     */
    public CommonHistoryList(T value, int depth)
    {
        this(depth);
        add(value);
    }

    /**
     * Builds new list
     *
     * @param depth Maximum list size
     */
    public CommonHistoryList(int depth)
    {
        if (depth < 1) {
            throw new IllegalArgumentException(
                "Depth must be at least 1, but received " + depth
            );
        }

        this.depth = depth;
    }

    /**
     * Adds new entry to the list
     *
     * @param value value to add
     */
    @Override
    public void add(T value)
    {
        synchronized (lock) {
            size++;
            newest = new Node<>(value, newest);
            inserted++;

            // Cross linking
            if (newest.older != null) {
                newest.older.newer = newest;
            }
            // Last one
            if (oldest == null) {
                oldest = newest;
            }

            if (size > depth) {
                // Cleaning oldest entry
                size--;
                oldest.newer.older = null;
                oldest = oldest.newer;
            }
        }
    }

    /**
     * @return Last added value
     */
    @Override
    public T getValue()
    {
        return newest.value;
    }

    /**
     * @return List of values in the history
     */
    @Override
    public List<T> asList()
    {
        List<T> answer = new ArrayList<>();
        Node<T> node;

        // Cloning newest
        node = newest;
        while (node != null) {
            answer.add(node.value);
            node = node.older;
        }

        return answer;
    }

    /**
     * @return Current size
     */
    @Override
    public int size()
    {
        return this.size;
    }

    /**
     * @return Configured depth
     */
    @Override
    public int depth()
    {
        return this.depth;
    }
}
