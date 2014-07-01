package eos.collections;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TimeSampledHistoryList <T>
{

    public static final long sampleRateMinute = 60000;
    public static final long sampleRateSecond = 10000;

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

    private class Node
    {
        T value;
        long sample;

        private Node(T value, long sample) {
            this.value = value;
            this.sample = sample;
        }
    }

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

    Node create(long sample)
    {
        return new Node(provider.provide(), sample);
    }

    @SuppressWarnings("unchecked")
    T extract(Object from)
    {
        return ((Node) from).value;
    }

    public long currentSample()
    {
        return System.currentTimeMillis() / sampleRate;
    }

    public T getValue() {
        return syncAndGetCurrent();
    }

    @SuppressWarnings("unchecked")
    public List<T> asList() {
        syncAndGetCurrent();
        synchronized (lock) {
            return Arrays
                    .asList(samples)
                    .stream()
                    .map(o -> ((Node) o).value)
                    .collect(Collectors.toList());
        }
    }

    public int depth() {
        return this.depth;
    }

    public static interface Provider<T>
    {
        public T provide();
    }
}
