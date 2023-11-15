package slatepowered.slate.logging;

import java.util.function.Supplier;

/**
 * Wraps any logger types.
 */
public interface Logger {

    /* Information */
    void info(Object... msg);
    default void info(Supplier<Object> msg) { info(msg.get()); }

    /* Warnings */
    void warn(Object... msg);
    default void warn(Supplier<Object> msg) { warn(msg.get()); }

    default void warning(Object... msg) {
        warn(msg);
    }

    default void warning(Supplier<Object> msg) {
        warn(msg);
    }

    /* Relatively unimportant errors */
    void error(Object... msg);
    default void error(Supplier<Object> msg) { error(msg); }

    /* Severe errors */
    void severe(Object... msg);
    default void severe(Supplier<Object> msg) { severe(msg); }

    /* Fatal errors */
    void fatal(Object... msg);
    default void fatal(Supplier<Object> msg) { fatal(msg); }

    /* Debug messages */
    void debug(Object... msg);
    default void debug(Supplier<Object> msg) { if (Logging.DEBUG) debug(msg.get()); }

    /**
     * Create a logger which doesn't do anything when the methods
     * are called.
     *
     * @return The voiding logger.
     */
    static Logger voiding() {
        return new Logger() {
            @Override
            public void info(Object... msg) {

            }

            @Override
            public void warn(Object... msg) {

            }

            @Override
            public void error(Object... msg) {

            }

            @Override
            public void severe(Object... msg) {

            }

            @Override
            public void fatal(Object... msg) {

            }

            @Override
            public void debug(Object... msg) {

            }
        };
    }

}
