package eos.type;

/**
 * Returns all combinations for logging
 */
public interface EosKeyCombinator
{
    /**
     * Returns all combination of keys
     *
     * @param origin Original key
     * @return Array of possible combinations
     */
    EosKey[] getCombinations(EosKey origin);
}
