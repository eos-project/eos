package eos.observers;

import eos.type.EosKey;

public class ObservingEvent
{
    final EosKey key;

    public ObservingEvent(EosKey key) {
        this.key = key;
    }

    public EosKey getKey() {
        return key;
    }
}
