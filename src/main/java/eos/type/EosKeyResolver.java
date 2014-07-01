package eos.type;

/**
 * Resolves eos key from provided string
 */
public interface EosKeyResolver
{
    /**
     * Returns new EosKey built from string
     *
     * @param source Source string
     * @return Eos key
     */
    EosKey resolve(String source);
}
