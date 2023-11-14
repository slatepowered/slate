package slatepowered.slate.packages.local;

import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.ResolvedPackage;

import java.nio.file.Path;

/**
 * A local package which consists of one file.
 */
public class LocalFilePackage extends LocalPackage {

    private final Path theFile;

    public LocalFilePackage(PackageManager manager, ResolvedPackage<?, ?> key, Path path, Path theFile) {
        super(manager, key, path);
        this.theFile = theFile;
    }

    /**
     * The single file path.
     *
     * @return The file path.
     */
    public Path getFile() {
        return theFile;
    }

}
