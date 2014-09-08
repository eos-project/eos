package eos.server;

/**
 * Exception to throw on wrong request
 */
public class WrongRequestException extends Exception
{
    public WrongRequestException() {
    }

    public WrongRequestException(String message) {
        super(message);
    }
}
