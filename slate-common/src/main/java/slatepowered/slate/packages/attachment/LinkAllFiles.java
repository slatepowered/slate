package slatepowered.slate.packages.attachment;

import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.veru.io.FileUtil;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

// Links all given files from the installed package to a destination
// in the node directory
public class LinkAllFiles<P extends LocalPackage> extends PackageAttachment<P> {

    /**
     * The directory to place the files in.
     */
    protected String destinationDirectory;

    /**
     * Whether to recursively link all files, not linking any directories.
     */
    protected boolean recursive;

    public LinkAllFiles() {

    }

    public LinkAllFiles(PackageKey<P> fromPackage, String destinationDirectory, boolean recursive) {
        super(fromPackage);
        this.destinationDirectory = destinationDirectory;
    }

    @Override
    protected void install0(PackageManager packageManager, ManagedNode node, Path nodePath, P localPackage) {
        try {
            final Path srcDir = localPackage.getPath();
            final Path destDir = nodePath.resolve(destinationDirectory);
            FileUtil.createDirectoryIfAbsent(destDir);

            Stream<Path> stream = !recursive ? Files.list(localPackage.getPath()) :
                    Files.walk(localPackage.getPath()).filter(p -> !Files.isDirectory(p));
            stream = stream.parallel();

            // link the files
            stream.forEach(srcFile -> {
                try {
                    Path destFile = destDir.resolve(srcDir.relativize(srcFile));
                    FileUtil.createDirectoryIfAbsent(destFile.getParent());

                    // check for directory
                    if (Files.isDirectory(srcFile)) {
                        Files.createSymbolicLink(destFile, srcFile);
                    } else {
                        Files.createLink(destFile, srcFile);
                    }
                } catch (Throwable t) {
                    Throwables.sneakyThrow(t);
                }
            });
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }
    }

}
