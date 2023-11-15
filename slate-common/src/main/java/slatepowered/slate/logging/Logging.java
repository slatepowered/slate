package slatepowered.slate.logging;

/**
 * Common logging utilities for consistent logging.
 */
public class Logging {

    /**
     * Whether to do debug logging.
     */
    public static final boolean DEBUG;

    /**
     * The current set logger provider.
     */
    private static LoggerProvider provider;

    static {
        DEBUG = Boolean.parseBoolean(System.getProperty("slate.debug", "false"));
    }

    public static void setProvider(LoggerProvider provider1) {
        if (provider != null)
            return; // silently fail
        provider = provider1;
    }

    /**
     * Get the currently active logger provider.
     *
     * @return The logger provider.
     */
    public static LoggerProvider getProvider() {
        return provider;
    }

    /**
     * Get the consistently configured logger for the given name.
     *
     * @param name The name.
     * @return The logger.
     */
    public static Logger getLogger(String name) {
        return provider != null ? provider.getLogger(name) : Logger.voiding();
    }

}
