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
     * The resolved package key.
     */
    private ResolvedPackage resolvedKey;

    /**
     * The directory of this package.
     */
    private final Path path;

    public synchronized ResolvedPackage getResolvedKey() {
        return resolvedKey;
    }

    public synchronized LocalPackage resolvedKey(ResolvedPackage resolvedKey) {
        this.resolvedKey = resolvedKey;
        return this;
    }

}
