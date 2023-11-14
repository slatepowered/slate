package slatepowered.slate.packages.local;

import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.ResolvedPackage;

import java.nio.file.Path;

/**
 * A local package which consists of one file.
 */
public class LocalFilesPackage extends LocalPackage {

    public LocalFilesPackage(PackageManager manager, ResolvedPackage<?, ?> key, Path path) {
        super(manager, key, path);
    }

}
