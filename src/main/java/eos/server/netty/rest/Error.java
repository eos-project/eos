package eos.server.netty.rest;

public class Error implements RestResponse
{
    final int code;
    final String id;
    final String message;
    final String url;

    public Error(int code, String id, String message, String url) {
        this.code    = code;
        this.id      = id == null ? "" : id;
        this.message = message == null ? "" : message;
        this.url     = url == null ? "" : message;
    }

    public int getCode() {
        return code;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }

    public String toJson()
    {
        return String.format(
                "{\n\t\"id\": \"%s\",\n\t\"message\": \"%s\",\n\t\"url\": \"%s\"\n}",
                getId(),
                getMessage(),
                getUrl()
        );
    }

    @Override
    public String toString() {
        return toJson();
    }

    @Override
    public String getContentType() {
        return "application/json; charset=UTF-8";
    }
}
