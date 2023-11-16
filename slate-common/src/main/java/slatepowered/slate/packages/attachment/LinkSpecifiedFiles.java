package slatepowered.slate.packages.attachment;

import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.veru.data.Pair;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Links the given files from the installed package to a destination
// in the node directory
public class LinkSpecifiedFiles<P extends LocalPackage> extends PackageAttachment<P> {

    /**
     * The list of files to link, with the first element being
     * the source file path and the second element being the destination
     * file path in the node directory.
     */
    protected List<Pair<String, String>> links;

    public LinkSpecifiedFiles() {

    }

    public LinkSpecifiedFiles(PackageKey<P> key, List<Pair<String, String>> links) {
        super(key);
        this.links = links;
    }

    @Override
    protected void install0(PackageManager packageManager, ManagedNode node, Path nodePath, P localPackage) {
        try {
            for (Pair<String, String> pair : links) {
                Path srcFile = localPackage.getPath().resolve(pair.getFirst());
                Path dstFile = nodePath.resolve(pair.getSecond());

                if (!Files.exists(srcFile))
                    continue;

                // check for directory
                if (Files.isDirectory(srcFile)) {
                    Files.createSymbolicLink(dstFile, srcFile);
                } else {
                    Files.createLink(dstFile, srcFile);
                }
            }
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }
    }

}
