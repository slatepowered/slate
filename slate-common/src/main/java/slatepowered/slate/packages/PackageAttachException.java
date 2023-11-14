package slatepowered.slate.packages;

import java.io.PrintStream;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Signals that the application of a package attachment raised an error.
 */
public class PackageAttachException extends RuntimeException {

    /**
     * The attachment which failed.
     */
    protected PackageAttachment<?> attachment;

    public PackageAttachException() {

    }

    public PackageAttachException(String message) {
        super(message);
    }

    public PackageAttachException(String message, Throwable cause) {
        super(message, cause);
    }

    public PackageAttachException(Throwable cause) {
        super(cause);
    }

    public PackageAttachException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PackageAttachException forAttachment(PackageAttachment<?> attachment) {
        this.attachment = attachment;
        return this;
    }

    @Override
    public void printStackTrace(PrintStream s) {
        if (attachment != null) {
            s.println("when attaching package: " + attachment.getSourcePackage() + " (using " + attachment + ")");
        }

        super.printStackTrace(s);
    }

}
