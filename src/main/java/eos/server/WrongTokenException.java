package eos.server;

/**
 * Exception to throw on wrong token
 */
public class WrongTokenException extends Exception
{
    final String token;

    public WrongTokenException(String token)
    {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
