package slatepowered.slate.logging;

import java.util.logging.Logger;

/**
 * Common logging utilities for consistent logging.
 */
public class Logging {

    /**
     * Whether to do debug logging.
     */
    public static final boolean DEBUG;

    static {
        DEBUG = Boolean.parseBoolean(System.getProperty("slate.debug", "false"));
    }

    public static Logger getLogger(String name) {
        return Logger.getLogger(name);
    }

}
