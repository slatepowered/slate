package slatepowered.slate.packages;

import lombok.Data;

import java.nio.file.Path;

/**
 * A locally managed package for the contained package key.
 */
@Data
public class LocalPackage {

    /**
     * The local package manager.
     */
    private final PackageManager manager;

    /**
     * The package key which identifies this package.
     */
    private final PackageKey key;

    /**
     * The directory of this package.
     */
    private final Path path;

}
