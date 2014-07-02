package eos.render.out;

import eos.render.AbstractContext;
import eos.render.ContextPlain;
import eos.render.Renderable;
import eos.render.util.ImplicitContext;

import java.io.PrintStream;

public class Console
{
    final PrintStream stream;
    final AbstractContext ctx;

    static boolean ansiCheck = false;
    static boolean ansiEnabled = false;

    public Console()
    {
        this(System.out);
    }

    public Console(PrintStream target)
    {
        stream = System.out;
        ctx    = new ContextPlain(new ImplicitContext(stream));
    }

    public static boolean isAnsi()
    {
        if (!ansiCheck) {
            ansiCheck = true;
            ansiEnabled = System.getenv("COLORTERM") != null;
        }
        return ansiEnabled;
    }

    public Console println(String[] oo)
    {
        for (String s : oo) {
            println(s);
        }

        return this;
    }

    public Console print(Object o)
    {
        if (o == null) {
            return this;
        } else if (o instanceof Renderable) {
            ((Renderable) o).render(ctx);
        } else if (o instanceof Color) {
            if (isAnsi()) {
                stream.print(o);
            }
        } else {
            stream.print(o);
        }

        return this;
    }

    public Console print(Object o, Color color)
    {
        return  print(color).print(o).print(Color.RESET);
    }

    public Console print(Object[] oo) {
        for (Object o : oo) {
            print(o);
        }

        return this;
    }

    public Console println(Object o)
    {
        return this.print(o).nl();
    }

    public Console nl()
    {
        stream.print("\n");
        return this;
    }

    public static enum Color
    {
        RESET("\u001B[0m"),
        FG_BLUE("\u001B[34m"),
        FG_RED("\u001B[31m"),
        FG_GREEN("\u001B[32m"),
        FG_YELLOW("\u001B[33m"),
        FG_PURPLE("\u001B[35m"),
        FG_CYAN("\u001B[36m"),
        FG_WHITE("\u001B[37m"),
        BOLD("\u001B[1m")
        ;

        final String code;

        Color(final String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
