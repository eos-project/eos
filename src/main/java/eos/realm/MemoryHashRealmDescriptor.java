package eos.realm;

import eos.type.EosKey;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;

public class MemoryHashRealmDescriptor extends HashMap<String, String> implements RealmDescriptor
{
    @Override
    public boolean allowed(EosKey key, String nonce, String payload, String signature) {
        return allowed(key.getRealm(), nonce, payload, signature);
    }

    @Override
    public boolean allowed(String realm, String nonce, String payload, String signature) {
        if (realm.equals("*") || realm.equals("test")) {
            return true;
        }

        if (!containsKey(realm)) {
            return false;
        }

        // Concatenating
        String concat = nonce.trim() + payload.trim() + get(realm).trim();

        // Validating signature
        return DigestUtils.sha256Hex(concat).equals(signature);
    }
}
