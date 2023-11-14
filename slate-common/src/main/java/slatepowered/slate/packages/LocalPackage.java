package slatepowered.slate.packages;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

/**
 * A locally managed package for the contained package key.
 */
@Getter
@RequiredArgsConstructor
public abstract class LocalPackage {

    /**
     * The local package manager.
     */
    private final PackageManager manager;

    /**
     * The package key which identifies this package.
     */
    private final ResolvedPackage<?, ?> key;

    /**
     * The directory of this package.
     */
    private final Path path;

}
