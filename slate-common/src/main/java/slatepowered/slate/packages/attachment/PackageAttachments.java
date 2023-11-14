package slatepowered.slate.packages.attachment;

import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;

import java.util.Arrays;

/**
 * Common package attachment implementations.
 */
public class PackageAttachments {

    /**
     * Copies files from a package to the node's working/data directory.
     *
     * @return The builder.
     */
    public static <P extends LocalPackage> CopyMatchingFiles.CopyMatchingFilesBuilder<P, ?, ?> copyFiles() {
        return CopyMatchingFiles.builder();
    }

    /**
     * (Symbolically) links files from a package to the node's working/data directory.
     *
     * @return The builder.
     */
    public static <P extends LocalPackage> LinkMatchingFiles.LinkMatchingFilesBuilder<P, ?, ?> linkFiles() {
        return LinkMatchingFiles.builder();
    }

    /**
     * Combine all package attachments to be executed for the same package.
     *
     * @param attachments The attachments.
     * @return The compound attachment.
     */
    @SafeVarargs
    public static <P extends LocalPackage> CompoundPackageAttachment<P> all(PackageAttachment<P>... attachments) {
        return new CompoundPackageAttachment<>(Arrays.asList(attachments));
    }

}
