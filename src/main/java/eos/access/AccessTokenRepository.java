package eos.access;

import eos.type.EosKey;

public interface AccessTokenRepository
{
    boolean isAllowedRead(String token, EosKey key);
    boolean isAllowedWrite(String token, EosKey key);
    String getCryptSecret(String token);
}
