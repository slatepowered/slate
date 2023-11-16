package slatepowered.slate.plugin;

/**
 * Thrown when an attempt is made to load a plugin again.
 */
public class DuplicatePluginException extends RuntimeException {

    /**
     * The ID of the plugin.
     */
    private String id;

    public DuplicatePluginException() {

    }

    public DuplicatePluginException(String message) {
        super(message);
    }

    public DuplicatePluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatePluginException(Throwable cause) {
        super(cause);
    }

    public DuplicatePluginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DuplicatePluginException id(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

}
