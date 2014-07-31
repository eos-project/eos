package eos.collections;

import java.util.ArrayList;
import java.util.List;

public class TimeSampledHistoryList <T>
{

    public static final long sampleRateMinute = 60000;
    public static final long sampleRateSecond = 1000;

    /**
     * Configured sample rate
     */
    final long sampleRate;

    final Provider<T> provider;

    /**
     * Configured maximum size of the list
     */
    final int depth;

    /**
     * Lock
     */
    final Object lock = new Object();

    /**
     * Data
     */
    Object[] samples;

    /**
     * Helper class, used to store data inside history list
     */
    private class Node
    {
        T value;
        long sample;

        private Node(T value, long sample) {
            this.value = value;
            this.sample = sample;
        }
    }

    /**
     * Constructor
     *
     * @param samplingRate Time in millisecond in each sample
     * @param samples      Amount of samples
     * @param provider     Data provider, used to create new content inside samples
     */
    public TimeSampledHistoryList(long samplingRate, int samples, Provider<T> provider)
    {
        this.depth      = samples;
        this.sampleRate = samplingRate;
        this.provider   = provider;
        //noinspection unchecked
        this.samples    = new Object[samples];

        long current = currentSample();
        for (int i=0; i < depth; i++) {
            this.samples[i] = create(current - i);
        }
    }

    /**
     * @return Current sample
     */
    @SuppressWarnings("unchecked")
    T syncAndGetCurrent()
    {
        synchronized (lock)
        {
            long current = currentSample();
            Node last    = (Node) samples[0];
            if (last.sample == current) {
                return last.value;
            }

            long delta = current - last.sample;

            // Need to resync
            if (delta > depth) {
                // Full resync
                for (int i=0; i < depth; i++) {
                    this.samples[i] = create(current - i);
                }
            } else {
                // Partial move
                for (int i = depth - 1; i >= 0; i--) {
                    if (i >= delta) {
                        this.samples[i] = this.samples[(int) (i - delta)];
                    } else  {
                        this.samples[i] = create(current - i);
                    }
                }
            }

            return ((Node)samples[0]).value;
        }
    }

    /**
     * Creates new node for sample
     *
     * @param sample Sample index
     * @return New node
     */
    Node create(long sample)
    {
        return new Node(provider.provide(), sample);
    }

    /**
     * @return Current sample index
     */
    public long currentSample()
    {
        return System.currentTimeMillis() / sampleRate;
    }

    /**
     * @return Current value
     */
    public T getValue() {
        return syncAndGetCurrent();
    }

    /**
     * @return All samples as list
     */
    @SuppressWarnings("unchecked")
    public List<T> asList() {
        syncAndGetCurrent();
        List<T> answer = new ArrayList<>();
        synchronized (lock) {
            for (Object o : samples) {
                answer.add(((Node) o).value);
            }
        }

        return answer;
    }

    /**
     * Describes structure that must provide entries for new samples
     *
     * @param <T> Any type
     */
    public static interface Provider<T>
    {
        public T provide();
    }
}
