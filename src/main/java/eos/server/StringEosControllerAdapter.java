package eos.server;

import eos.EosController;
import eos.observers.IncrementObserver;
import eos.observers.LoggersObserver;
import eos.realm.RealmDescriptor;
import eos.type.EosKey;
import eos.type.EosKeyResolver;

public class StringEosControllerAdapter
{
    final EosController ctrl;
    final EosKeyResolver resolver;
    final RealmDescriptor realms;

    public StringEosControllerAdapter(EosController ctrl, RealmDescriptor descriptor, EosKeyResolver resolver) {
        this.ctrl     = ctrl;
        this.realms   = descriptor;
        this.resolver = resolver;
    }

    public void process(String[] args) throws WrongTokenException, WrongRequestException {
        if (args == null || args.length < 3) {
            throw new WrongRequestException("Expecting at least 3 arguments");
        }

        String nonce          = args[0];
        String tokenCandidate = args[1];
        String keyCandidate   = args[2];
        String payload        = args.length > 3 ? args[3] : "";

        if (tokenCandidate.contains("+")) {
            String[] parts = tokenCandidate.split("\\+");
            keyCandidate   = parts[0] + "+" + keyCandidate;
            tokenCandidate = parts[1];
        }

        // Creating key
        EosKey key = resolver.resolve(keyCandidate);

        // Validating
        if (!realms.allowed(key, nonce, payload, tokenCandidate)) {
            throw new WrongTokenException(tokenCandidate);
        }

        if (key.schemaEquals(EosKey.Schema.inc)) {
            doMakeIncrement(key, payload);
        } else if (key.schemaEquals(EosKey.Schema.log)) {
            doMakeLog(key, payload);
        } else {
            throw new RuntimeException("Not implemented");
        }
    }

    void doMakeIncrement(EosKey key, String payload) throws WrongTokenException
    {
        long delta = 1;
        if (payload.trim().length() > 0) {
            delta = Long.parseLong(payload);
        }
        ctrl.sendEvent(new IncrementObserver.Event(key, delta));
    }

    void doMakeLog(EosKey key, String payload) throws WrongTokenException
    {
        if (payload.length() == 0) {
            payload = "Empty log message";
        }

        ctrl.sendEvent(new LoggersObserver.Event(key, payload));
    }
}
