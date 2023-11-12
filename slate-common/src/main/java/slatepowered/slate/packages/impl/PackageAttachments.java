package slatepowered.slate.packages.impl;

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
    public static CopyMatchingFiles.CopyMatchingFilesBuilder<?, ?> copyFiles() {
        return CopyMatchingFiles.builder();
    }

    /**
     * (Symbolically) links files from a package to the node's working/data directory.
     *
     * @return The builder.
     */
    public static LinkMatchingFiles.LinkMatchingFilesBuilder<?, ?> linkFiles() {
        return LinkMatchingFiles.builder();
    }

    /**
     * Combine all package attachments to be executed for the same package.
     *
     * @param attachments The attachments.
     * @return The compound attachment.
     */
    public static CompoundPackageAttachment all(PackageAttachment... attachments) {
        return new CompoundPackageAttachment(Arrays.asList(attachments));
    }

}
