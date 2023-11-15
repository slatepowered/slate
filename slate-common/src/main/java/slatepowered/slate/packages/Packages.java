package slatepowered.slate.packages;

import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.packages.attachment.*;
import slatepowered.slate.packages.key.JavaPackageKey;
import slatepowered.slate.packages.key.URLFilesDownload;
import slatepowered.veru.data.Pair;
import slatepowered.veru.runtime.JavaVersion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Helper functions for packages.
 */
public final class Packages {

    /**
     * Creates a new file download package key from the given
     * varargs array. Each even element is expected to be a URL
     * and the next (odd) element is the filename that file should be
     * downloaded to.
     *
     * @param files The varargs array.
     * @return The download key.
     */
    public static URLFilesDownload download(String... files) {
        return URLFilesDownload.download(files);
    }

    /**
     * Creates a new file download package key from the given
     * list. Each pair in the list is expected to have the first
     * element be the download URL and the second element the output file name.
     *
     * @param files The list of pairs.
     * @return The download key.
     */
    public static URLFilesDownload download(List<Pair<String, String>> files) {
        return new URLFilesDownload(files);
    }

    /**
     * Requires a JDK installation of the given version.
     *
     * @param version The version.
     * @return The JDK package key.
     */
    public static JavaPackageKey jdk(JavaVersion version) {
        return JavaPackageKey.jdk(version);
    }

    /**
     * Requires any installation of the given Java version.
     *
     * @param version The version.
     * @return The Java installation package key.
     */
    public static JavaPackageKey java(JavaVersion version) {
        return JavaPackageKey.jdk(version);
    }

    /**
     * Copies files from a package to the node's working/data directory.
     *
     * @return The builder.
     */
    public static <P extends LocalPackage> CopyMatchingFiles<P> copyFiles(PackageKey<P> key, BiFunction<Path, Path, Path> resolver) {
        return new CopyMatchingFiles<>(key, resolver);
    }

    /**
     * (Symbolically) links files from a package to the node's working/data directory.
     *
     * @return The builder.
     */
    public static <P extends LocalPackage> LinkMatchingFiles<P> linkFiles(PackageKey<P> key, BiFunction<Path, Path, Path> resolver) {
        return new LinkMatchingFiles<>(key, resolver);
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

    /**
     * Immediately loads the given files as libraries on the receiver
     * of the attachments, not on the node it is installed to.
     *
     * This will fail if the node it is received by is not the node it is
     * supposed to be installed on.
     *
     * @param files The files.
     * @return The attachment.
     */
    public static <P extends LocalPackage> LoadAttachment<P> loadLibrariesImmediate(PackageKey<P> key, String... files) {
        return new LoadAttachment<>(key, Arrays.asList(files), false);
    }

    /**
     * Immediately loads the given files as libraries and plugins if possible on the receiver
     * of the attachments, not on the node it is installed to.
     *
     * This will fail if the node it is received by is not the node it is
     * supposed to be installed on.
     *
     * @param files The files.
     * @return The attachment.
     */
    public static <P extends LocalPackage> LoadAttachment<P> loadImmediate(PackageKey<P> key, String... files) {
        return new LoadAttachment<>(key, Arrays.asList(files), true);
    }

    // Flattens the given attachment and it's dependencies into the given list,
    // also making sure there are no duplicates
    @SuppressWarnings("unchecked")
    private static void flattenAttachment(List<PackageAttachment<LocalPackage>> list,
                                          PackageAttachment<?> attachment) {
        if (list.contains(attachment)) {
            return;
        }

        for (PackageAttachment<?> dependency : attachment.dependencies()) {
            flattenAttachment(list, dependency);
        }

        list.add((PackageAttachment<LocalPackage>)attachment);
    }

    /**
     * Internal helper function.
     * Attaches all given attachments, in order, to the given node.
     *
     * @param packageManager The package manager.
     * @param attachments The list of attachments.
     * @param node The node to attach to.
     * @param path The node directory.
     */
    public static CompletableFuture<List<Throwable>> attachAll(PackageManager packageManager,
                                                               List<PackageAttachment<?>> attachments,
                                                               ManagedNode node,
                                                               Path path,
                                                               ManagedNode hostNode,
                                                               Path hostPath) {
        List<PackageAttachment<LocalPackage>> flattenedAttachmentList = new ArrayList<>();
        for (PackageAttachment<?> attachment : attachments) {
            flattenAttachment(flattenedAttachmentList, attachment);
        }

        // start applying attachments and register all potential
        // errors into a list
        CompletableFuture<List<Throwable>> future = new CompletableFuture<>();
        Vector<Throwable> errors = new Vector<>();
        for (PackageAttachment<LocalPackage> attachment : flattenedAttachmentList) {
            PackageTarget target = attachment instanceof TargetedPackageAttachment ?
                    ((TargetedPackageAttachment<?>)attachment).getTarget() :
                    PackageTarget.NODE;

            attachment.getSourcePackage().
                    findOrInstall(packageManager)
                    .thenApply(localPackage -> {
                        // install attachment
                        attachment.install(
                                packageManager,
                                target == PackageTarget.HOST ? hostNode : node,
                                target == PackageTarget.HOST ? hostPath : path,
                                localPackage
                        );

                        return null;
                    })
                    .whenComplete((localPackage, throwable) -> {
                        errors.add(throwable == null ? null : new PackageAttachException(throwable).forAttachment(attachment));
                        if (errors.size() == flattenedAttachmentList.size()) {
                            future.complete(errors);
                        }
                    });
        }

        return future;
    }

}
