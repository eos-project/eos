package eos.access;

import eos.type.EosKey;

/**
 * Token repository, that grants access to any key
 */
public class GrantAllTokenRepository implements AccessTokenRepository
{
    @Override
    public boolean isAllowedRead(String token, EosKey key) {
        return true;
    }

    @Override
    public boolean isAllowedWrite(String token, EosKey key) {
        return true;
    }

    @Override
    public String getCryptSecret(String token) {
        return null;
    }
}
