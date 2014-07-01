package eos.type;

public interface KeyFilter
{
    /**
     * Returns true, if provided key matches filter
     *
     * @param key Key to match against filter
     */
    boolean matches(EosKey key);
}
