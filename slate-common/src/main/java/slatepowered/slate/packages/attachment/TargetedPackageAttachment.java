package slatepowered.slate.packages.attachment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.PackageTarget;

import java.nio.file.Path;

/**
 * Represents a package attachment targeted to a specific domain.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TargetedPackageAttachment<P extends LocalPackage> extends PackageAttachment<P> {

    /**
     * The attachment to be executed.
     */
    protected PackageAttachment<P> attachment;

    /**
     * The attachment target.
     */
    protected PackageTarget target;

    @Override
    public void install(PackageManager packageManager, ManagedNode node, Path nodePath, P localPackage) {
        attachment.install(packageManager, node, nodePath, localPackage);
    }

}
