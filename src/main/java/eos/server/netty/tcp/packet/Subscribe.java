package eos.server.netty.tcp.packet;

import java.io.Serializable;

public class Subscribe implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Filter pattern to subscribe
     */
    public String filterPattern;
}
