package eos.client;

import eos.render.out.Console;
import eos.observers.IncrementObserver;
import eos.observers.LoggersObserver;
import eos.observers.Observer;
import eos.observers.ObservingEvent;
import eos.type.KeyFilter;

import java.util.ArrayList;

public class CliStream implements Observer
{
    final Console console;
    final KeyFilter filter;

    public CliStream(KeyFilter filter)
    {
        this.console = new Console();
        this.filter  = filter;
    }

    @Override
    public void report(ObservingEvent event) {
        if (filter != null && !filter.matches(event.getKey())) {
            // Not matched
            return;
        }

        ArrayList<Object> list = new ArrayList<>();
        list.add("[");
        list.add(Console.Color.FG_CYAN);
        list.add(event.getKey().getSchema().toString());
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
            list.add("\n");
            list.add(Console.Color.FG_GREEN);
            list.add(((LoggersObserver.Event) event).getLine());
            list.add(Console.Color.RESET);
        }

        console.print(list.toArray()).nl();
    }

}
