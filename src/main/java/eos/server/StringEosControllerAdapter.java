package eos.server;

import eos.EosController;
import eos.access.AccessTokenRepository;
import eos.observers.IncrementObserver;
import eos.observers.LoggersObserver;
import eos.type.EosKey;

public class StringEosControllerAdapter
{
    private final EosController ctrl;
    private final AccessTokenRepository tokens;

    public StringEosControllerAdapter(EosController ctrl, AccessTokenRepository tokens) {
        this.ctrl   = ctrl;
        this.tokens = tokens;
    }

    public void process(String[] args) throws WrongTokenException
    {
        String token = args[0];
        EosKey key   = EosKey.parse(args[1]);

        if (key.schemaEquals(EosKey.Schema.inc)) {
            doMakeIncrement(key, token, args);
        } else if (key.schemaEquals(EosKey.Schema.log)) {
            doMakeLog(key, token, args);
        } else {
            throw new RuntimeException("Not implemented");
        }
    }

    void doMakeIncrement(EosKey key, String token, String[] args) throws WrongTokenException
    {
        long delta = 1;
        if (args.length == 3) {
            delta = Long.parseLong(args[2]);
        }
        ctrl.sendEvent(token, new IncrementObserver.Event(key, delta));
    }

    void doMakeLog(EosKey key, String token, String[] args) throws WrongTokenException
    {
        String message;
        if (args.length > 2) {
            message = args[2];
        } else {
            message = "Empty log message";
        }

        ctrl.sendEvent(token, new LoggersObserver.Event(key, message));
    }
}
