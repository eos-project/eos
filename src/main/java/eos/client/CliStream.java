package eos.client;

import eos.observers.IncrementObserver;
import eos.observers.LoggersObserver;
import eos.observers.Observer;
import eos.observers.ObservingEvent;
import eos.render.out.Console;
import eos.type.KeyFilter;
import eos.util.StringStartPatternFinder;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CliStream implements Observer
{
    static final SimpleDateFormat fullTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static final SimpleDateFormat partTime = new SimpleDateFormat("HH:mm:ss");
    static final StringStartPatternFinder sspf = new StringStartPatternFinder();

    final Console console;
    final KeyFilter filter;
    final JsonFactory factory;
    final long separatorThreshold = 3000;
    final long fullTimeThreshold  = 3600000;
    long lastOutput;

    public CliStream(KeyFilter filter)
    {
        this.console = new Console();
        this.filter  = filter;
        this.factory = new JsonFactory();
    }

    @Override
    public synchronized void report(ObservingEvent event) {
        if (filter != null && !filter.matches(event.getKey())) {
            // Not matched
            return;
        }

        long current = System.currentTimeMillis();
        long delta = current - lastOutput;

        ArrayList<Object> list = new ArrayList<>();
        if (delta > separatorThreshold) {
            // New log session
            list.add("\n\n\n      ------------------------------------------\n\n\n");
        }

        list.add(Console.Color.FG_CYAN);
        list.add("[" + event.getKey().getSchema().toString() + "] ");
        list.add(Console.Color.RESET);

        list.add("[");
        list.add(Console.Color.FG_PURPLE);
        if (delta > fullTimeThreshold) {
            list.add(fullTime.format(new Date()));
        } else if (delta > separatorThreshold) {
            list.add(partTime.format(new Date()));
        } else {
            list.add(String.format("+%.3fs", ((float) delta) / 1000f));
        }
        list.add(Console.Color.RESET);
        list.add("] ");

        if (event instanceof IncrementObserver.Event) {
            IncrementObserver.Event e = (IncrementObserver.Event) event;
            list.add("[");
            list.add(Console.Color.FG_GREEN);
            if (e.getDelta() == 1) {
                list.add("++");
            } else {
                list.add("+=" + e.getDelta());
            }
            list.add(Console.Color.RESET);
            list.add("] ");
        }

        list.add(event.getKey().toString());

        if (event instanceof LoggersObserver.Event) {
            String line = ((LoggersObserver.Event) event).getLine();
            list.add("\n");
            if (line.startsWith("{") && line.endsWith("}")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    showJson(mapper.readTree(factory.createJsonParser(line)), list);
                } catch (Exception e) {
                    list.add(Console.Color.FG_RED);
                    list.add(e.getMessage());
                    list.add("\n");
                    list.add(line);
                    list.add(Console.Color.RESET);
                }
            } else {
                list.add(Console.Color.FG_GREEN);
                list.add(line);
                list.add(Console.Color.RESET);
            }
        }

        console.print(list.toArray()).nl();
        lastOutput = current;
    }

    void showJson(JsonNode node, List<Object> list) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        if (!node.isObject()) {
            throw new RuntimeException("Not a object");
        }

        if (node.has("message")) {
            list.add(Console.Color.FG_YELLOW);
            list.add("[msg] ");
            list.add(Console.Color.FG_GREEN);
            list.add(node.get("message").toString());
            list.add(Console.Color.RESET);
            list.add("\n");
        }

        if (node.has("object")) {
            list.add(Console.Color.FG_YELLOW);
            list.add("[ctx] ");
            list.add(Console.Color.FG_GREEN);
            list.add(node.get("object").toString());
            list.add(Console.Color.RESET);
            list.add("\n");
        }

        if (node.has("exception")) {
            JsonNode n = node.get("exception");
            if (n.has("message")) {
                list.add(Console.Color.FG_YELLOW);
                list.add("[err] ");
                list.add(Console.Color.RESET);
                list.add(Console.Color.FG_GREEN);
                list.add(n.get("message").toString());
                list.add(Console.Color.RESET);
                list.add("\n");
            }
            if (n.has("trace") && n.get("trace").isArray()) {
                list.add(Console.Color.FG_YELLOW);
                list.add("[err] [trace]");
                list.add(Console.Color.RESET);
                list.add("\n");
                Iterator<JsonNode> el = n.get("trace").getElements();

                List<String> lineNumbers = new ArrayList<>();
                List<String> files       = new ArrayList<>();

                while (el.hasNext()) {
                    JsonNode x = el.next();
                    lineNumbers.add(x.get("line").toString());
                    files.add(x.get("file").toString());
                }

                // Repeating pattern
                String filePattern = sspf.findPattern(files.toArray(new String[files.size()]));

                for (int i = 0; i < files.size(); i++) {
                    list.add("      ");
                    list.add("Line ");
                    list.add(Console.Color.FG_CYAN);
                    list.add(lineNumbers.get(i));
                    list.add(Console.Color.RESET);
                    list.add(" @ ");
                    if (filePattern.length() > 0) {
                        list.add(Console.Color.FG_CYAN);
                        list.add("/");
                        list.add(Console.Color.FG_GREEN);
                        list.add(files.get(i).substring(filePattern.length()));
                    } else {
                        list.add(Console.Color.FG_GREEN);
                    }
                    list.add(Console.Color.RESET);
                    list.add("\n");
                }

                /*
                while (el.hasNext()) {
                    JsonNode x = el.next();
                    if (x.has("file") && x.has("line")) {
                        list.add(Console.Color.FG_YELLOW);
                        list.add("      ");
                        list.add(Console.Color.RESET);
                        list.add("Line ");
                        list.add(Console.Color.FG_CYAN);
                        list.add(x.get("line").toString());
                        list.add(Console.Color.RESET);
                        list.add(" @ ");
                        list.add(Console.Color.FG_GREEN);
                        list.add(x.get("file").toString());
                        list.add(Console.Color.RESET);
                        list.add("\n");
                    }
                }
                */
            }
        }

        Iterator<String> fn = node.getFieldNames();
        while (fn.hasNext()) {
            String field = fn.next();
            JsonNode n = node.get(field);
            if ("object".equals(field) || "message".equals(field)  || "exception".equals(field)) continue;

            list.add(Console.Color.FG_YELLOW);
            list.add("[var] [" + field + "] ");
            list.add(Console.Color.RESET);
            if (n.isObject() || n.isArray()) {
                list.add(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(n));
            } else if (n.isNumber()) {
                list.add(Console.Color.FG_CYAN);
                list.add(n.toString());
                list.add(Console.Color.RESET);
            } else {
                list.add(Console.Color.FG_GREEN);
                list.add(n.toString());
                list.add(Console.Color.RESET);
            }
            list.add("\n");
        }
    }

}
