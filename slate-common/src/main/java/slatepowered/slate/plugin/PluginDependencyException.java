package slatepowered.slate.plugin;

/**
 * Signals a problem with plugin dependencies.
 */
public class PluginDependencyException extends RuntimeException {

    public PluginDependencyException() {

    }

    public PluginDependencyException(String message) {
        super(message);
    }

    public PluginDependencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginDependencyException(Throwable cause) {
        super(cause);
    }

    public PluginDependencyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
