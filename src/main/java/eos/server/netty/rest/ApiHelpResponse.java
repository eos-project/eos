package eos.server.netty.rest;

import eos.App;

public class ApiHelpResponse implements RestResponse
{
    String content;

    public ApiHelpResponse() {
        StringBuilder sb = new StringBuilder();

        sb.append(App.signature);
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");

        sb.append("This is EOS Api welcome page.\n\n");

        sb.append("Commands\n");
        sb.append("========\n\n");
        sb.append(" * Find metrics. `/api/find/<mask>`. Example `/api/find/*`\n");
        sb.append(" * Get metric value. `/api/get/<name>`. Example `/api/get/inc://eos.core.server.udp.requests`\n");
        sb.append("\n\n");

        sb.append("Health\n");
        sb.append("======\n\n");
        sb.append(" * Processors  : " + Runtime.getRuntime().availableProcessors() + "\n");
        sb.append(" * Total memory: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "M\n");
        sb.append(" * Max memory  : " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "M\n");
        sb.append(" * Free memory : " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + "M\n");
        sb.append(" * Used memory : " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())  / 1024 / 1024 + "M\n");

        content = sb.toString();
    }

    @Override
    public int getCode() {
        return 200;
    }

    @Override
    public String toString() {
        return content;
    }

    @Override
    public String getContentType() {
        return "text/plain; charset=UTF-8";
    }
}
