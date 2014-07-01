package eos.type;

/**
 * Incremental metrics
 */
public interface LongIncrement extends EosEntry
{
    /**
     * Increments metric
     */
    void inc();

    /**
     * Adds arbitrary increment
     *
     * @param value Increment amount
     */
    void add(long value);

    /**
     * @return Current increment value
     */
    long getValue();
}
