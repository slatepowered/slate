package slatepowered.slate.packages;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.model.SharedNodeComponent;
import slatepowered.slate.packages.attachment.TargetedPackageAttachment;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Determines the way a package is installed onto a node.
 */
@AllArgsConstructor
@NoArgsConstructor
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

    /**
     * Get the attachments which the installation/attachment
     * of the package depends on.
     *
     * @return The list of dependencies.
     */
    public List<PackageAttachment<?>> dependencies() {
        return Collections.emptyList();
    }

    /**
     * Targets this package attachment to a specific domain.
     *
     * @return The attachment.
     */
    public TargetedPackageAttachment<P> targeted(PackageTarget target) {
        return new TargetedPackageAttachment<P>(this, target);
    }

}
