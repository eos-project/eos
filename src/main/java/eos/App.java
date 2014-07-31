package eos;

import eos.access.GrantAllTokenRepository;
import eos.client.CliStream;
import eos.client.netty.TcpClient;
import eos.filters.FilterFactory;
import eos.observers.ObservingPool;
import eos.observers.SynchronousObservingPool;
import eos.render.out.Console;
import eos.server.CommonEosController;
import eos.server.CommonEosRegistry;
import eos.server.netty.rest.RestServer;
import eos.server.netty.tcp.TcpServer;
import eos.server.netty.udp.UdpServer;
import eos.server.netty.ws.WsServer;
import eos.type.CachedEosKeyResolver;
import eos.type.EosKeyCombinator;
import eos.type.EosKeyResolver;
import eos.type.KeyFilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Eos startup class
 */
public class App implements Runnable
{
    // Defaults
    final int defaultRestPort   = 8085;
    final int defaultUdpPort    = 8087;
    final int defaultTcpPort    = 8088;
    final int defaultWsPort     = 8090;

    /**
     * Command-line arguments
     */
    final Map<String, String> args;
    /**
     * Output
     */
    final Console stdout;
    /**
     * Observers pool
     */
    final ObservingPool observerMaster;
    /**
     * Metric registry
     */
    final EosRegistry metricRegistry;
    /**
     * Main controller
     */
    final EosController metricController;
    /**
     * Eos key resolver
     */
    final EosKeyResolver resolver;
    /**
     * Eos key combinator
     */
    final EosKeyCombinator combinator;

    /**
     * Startup method
     *
     * @param args Command line arguments list
     * @throws Exception On any error
     */
    public static void main(String[] args) throws Exception
    {
        (new App(Arrays.asList(args))).run();
    }

    /**
     * Constructor
     *
     * @param arguments Command line arguments
     */
    private App(List<String> arguments) {
        // Copying argument
        this.args   = inflateArguments(arguments);

        // Creating console for output
        this.stdout = new Console();

        // Creating resolver & combinator
        CachedEosKeyResolver cachedEosKeyResolver = new CachedEosKeyResolver((byte) 6, 997, 997);
        this.resolver   = cachedEosKeyResolver;
        this.combinator = cachedEosKeyResolver;

        // Creating main eos registry
        this.metricRegistry   = new CommonEosRegistry(1000, this.combinator);

        // Creating main observer and registering a registry
        this.observerMaster   = new SynchronousObservingPool();
        this.observerMaster.register(metricRegistry);
        this.observerMaster.register(metricRegistry);

        // Creating controller
        this.metricController = new CommonEosController(
            metricRegistry,
            observerMaster,
            new GrantAllTokenRepository(),
            true
        );
    }

    /**
     * Reads arguments from list and maps them
     *
     * @param arguments Arguments list
     * @return Mapped arguments
     */
    public Map<String, String> inflateArguments(List<String> arguments)
    {
        Map<String, String> map = new HashMap<>(arguments.size());
        for (String a : arguments) {
            if (a.startsWith("--")) {
                String[] split = a.split("=");
                if (split.length == 1) {
                    map.put(split[0].substring(2), null);
                } else {
                    map.put(split[0].substring(2), split[1]);
                }
            } else if (a.startsWith("-")) {
                map.put(a.substring(1), null);
            } else {
                map.put(a, null);
            }
        }

        return map;
    }

    /**
     * Runs application
     */
    @Override
    public void run() throws RuntimeException
    {
        try {
            FilterFactory ff = new FilterFactory();

            if (args.size() == 0 || args.containsKey("help") || args.containsKey("h")) {
                this.help();
                return;
            }

            if (args.containsKey("udp")) {
                String portString = args.get("udp");
                int port = this.defaultUdpPort;
                if (portString != null) {
                    port = Integer.parseInt(portString);
                }

                this.udp(port);
            }

            if (args.containsKey("tcp")) {
                String portString = args.get("tcp");
                int port = this.defaultTcpPort;
                if (portString != null) {
                    port = Integer.parseInt(portString);
                }

                this.tcp(port);
            }

            if (args.containsKey("rest")) {
                String portString = args.get("rest");
                int port = this.defaultRestPort;
                if (portString != null) {
                    port = Integer.parseInt(portString);
                }

                this.rest(port);
            }

            if (args.containsKey("ws")) {
                String portString = args.get("ws");
                int port = this.defaultWsPort;
                if (portString != null) {
                    port = Integer.parseInt(portString);
                }

                this.ws(port);
            }

            if (args.containsKey("cli")) {
                String filterString = args.get("cli");
                this.cli(ff.getFilter(filterString));
            }

            if (args.containsKey("connect")) {
                String[] sub = args.get("connect").split(",");
                if (sub.length < 2) {
                    this.help();
                    return;
                }

                String host = sub[0];
                int port = Integer.parseInt(sub[1]);

                connect(host, port, null);
            }

        } catch (Exception e) {
            this.help();
            throw new RuntimeException(e);
        }
    }

    /**
     * Shows help page
     */
    void help()
    {
        stdout.nl().print(Console.Color.FG_CYAN).print(Console.Color.BOLD).print(signature).println(Console.Color.RESET);

        stdout.println(new String[]{
                "",
                "",
                "Usage: eos --<command>, [--<command2>, [... --<commandN>]]",
                "",
                "Commands:",
                "  --help | -h      displays this message",
                "  --rest[=port]    starts web server on port " + defaultRestPort,
                "  --tcp[=port]     starts tcp replica listener on port " + defaultUdpPort,
                "  --ws[=port]      starts web socket server on port " + defaultWsPort,
                "  --udp[=port]     starts udp listener on port " + defaultTcpPort,
                "  --cli[=filter]   cli streaming",
                "  --connect=host,port[,filter] connects to tcp replica server"
        });
    }

    /**
     * Starts REST server
     *
     * @param port Port
     * @throws Exception
     */
    void rest(int port) throws Exception
    {
        stdout.println("Starting REST server");
        (new Thread(new RestServer("localhost", port, metricRegistry,  metricController, resolver))).start();
        stdout.println("REST server listening on " + port);
    }

    /**
     * Starts UDP server
     *
     * @param port Port
     * @throws Exception
     */
    void udp(int port) throws Exception
    {
        stdout.println("Starting UDP listener");
        (new Thread(new UdpServer("0.0.0.0", port, metricRegistry,  metricController, resolver))).start();
        stdout.println("UDP server listening on " + port);
    }

    /**
     * Starts TCP replica server
     *
     * @param port Port
     * @throws Exception
     */
    void tcp(int port) throws Exception
    {
        stdout.println("Starting replica TCP listener");
        (new Thread(new TcpServer("localhost", port, metricRegistry, observerMaster))).start();
        stdout.println("TCP server listening on " + port);
    }

    /**
     * Starts web socket server
     *
     * @param port Port
     * @throws Exception
     */
    void ws(int port) throws Exception
    {
        stdout.println("Starting web socket server");
        (new Thread(new WsServer("0.0.0.0", port, metricRegistry, observerMaster))).start();
        stdout.println("Web socket server listening on " + port);
    }

    void connect(String host, int port, KeyFilter filter) throws Exception
    {
        stdout.println("Starting replica TCP client");
        (new Thread(new TcpClient(metricRegistry, observerMaster, host, port, filter, resolver))).start();
        stdout.println("TCP client online");
    }

    /**
     * Starts UDP listener
     *
     * @throws Exception
     */
    void cli(KeyFilter filter) throws Exception
    {
        stdout.println("Starting CLI stream listener");
        this.observerMaster.register(new CliStream(filter));
    }

    /**
     * Application signature
     */
    public final static String signature =
        "   __________             \n" +
        "   ___  ____/_____________\n" +
        "   __  __/  _  __ \\_  ___/\n" +
        "   _  /___  / /_/ /(__  ) \n" +
        "   /_____/  \\____//____/ ";

}
