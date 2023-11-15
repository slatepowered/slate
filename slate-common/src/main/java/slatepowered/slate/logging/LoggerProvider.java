package slatepowered.slate.logging;

import java.util.HashMap;
import java.util.Map;

public abstract class LoggerProvider {

    // The cache of created loggers
    protected final Map<String, Logger> loggerMap = new HashMap<>();

    /** Creates a new logger with the given name. */
    protected abstract Logger createLogger(String name);

    /**
     * Get or create a logger with the given name.
     *
     * @param name The name.
     * @return The logger instance with the given name.
     */
    public Logger getLogger(String name) {
        return loggerMap.computeIfAbsent(name, this::createLogger);
    }

}
