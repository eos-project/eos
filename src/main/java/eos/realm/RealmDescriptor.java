package eos.realm;

import eos.type.EosKey;

public interface RealmDescriptor
{
    boolean allowed(EosKey key, String nonce, String payload, String signature);
    boolean allowed(String realm, String nonce, String payload, String signature);
}
