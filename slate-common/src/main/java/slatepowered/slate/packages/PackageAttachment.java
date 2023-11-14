package slatepowered.slate.packages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.model.SharedNodeComponent;

import java.nio.file.Path;

/**
 * Determines the way a package is installed onto a node.
 */
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class PackageAttachment<P extends LocalPackage> implements SharedNodeComponent {

    /**
     * The package to attach to the node.
     */
    protected PackageKey<P> fromPackage;

    public PackageKey<P> getSourcePackage() {
        return fromPackage;
    }

    /**
     * Installs the package to the given node, this is
     * called locally on a cluster or other type of
     * node allocator/creator to install the required packages.
     *
     * @param packageManager The local package manager.
     * @param node The managed node to install it to.
     * @param nodePath The directory of the node.
     * @param localPackage The resolved local package.
     */
    public abstract void install(
            PackageManager packageManager,
            ManagedNode node,
            Path nodePath,
            P localPackage
    );

}
