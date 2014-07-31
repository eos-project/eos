package eos.access;

import eos.type.EosKey;

public interface AccessTokenRepository
{
    /**
     * Checks provided token access level
     *
     * @param token Token to check
     * @param key   Eos key
     * @return True if provided token has read access to provided key
     */
    boolean isAllowedRead(String token, EosKey key);

    /**
     * Checks provided token access level
     *
     * @param token Token to check
     * @param key   Eos key
     * @return True if provided token has write access to provided key
     */
    boolean isAllowedWrite(String token, EosKey key);

    /**
     * Returns crypt secret for provided token
     *
     * @param token Token
     * @return Crypt secret
     */
    String getCryptSecret(String token);
}
