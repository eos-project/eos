package eos.collections;

/**
 * Helper class to cache calculation values
 *
 * @param <K> Parameter type
 * @param <V> Return type
 */
public class CalculationCache<K, V>
{
    final int capacity;
    final Object[] cache;
    final Supplier<K, V> supplier;

    /**
     * Constructor
     *
     * @param capacity Capacity of cache
     * @param supplier Supplier, used to generate value on cache miss
     */
    public CalculationCache(int capacity, Supplier<K, V> supplier)
    {
        this.capacity = capacity;
        this.supplier = supplier;
        this.cache    = new Object[capacity];
    }

    /**
     * Returns value
     *
     * @param in Argument
     * @return Value
     */
    @SuppressWarnings("unchecked")
    public V get(K in) {
        int hash = in.hashCode() % capacity;
        if (cache[hash] == null) {
            cache[hash] = supplier.calculate(in);
        }

        return (V) cache[hash];
    }

    /**
     * Supplier interface
     *
     * @param <K> Parameter type
     * @param <V> Return type
     */
    public static interface Supplier<K, V>
    {
        /**
         * Calculates value
         *
         * @param in Argument
         * @return Value
         */
        V calculate(K in);
    }
}
