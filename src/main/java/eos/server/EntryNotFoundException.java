package eos.server;

public class EntryNotFoundException extends Exception
{
    final String name;

    public EntryNotFoundException(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
