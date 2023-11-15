package slatepowered.slate.logging;

import java.util.function.Supplier;

public abstract class AbstractLogger implements Logger {

    /**
     * The name of the logger.
     */
    protected final String name;

    public AbstractLogger(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // converts the given message array to
    // a string representation which can be used as
    // the string message
    protected String stringify(Object... msg) {
        StringBuilder b = new StringBuilder();
        for (Object o : msg) {
            b.append(o);
        }

        return b.toString();
    }

}
