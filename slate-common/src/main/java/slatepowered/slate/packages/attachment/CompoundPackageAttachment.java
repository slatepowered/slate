package slatepowered.slate.packages.attachment;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageManager;

import java.nio.file.Path;
import java.util.List;

// Links files from the installed package to a destination
// in the node directory
@AllArgsConstructor
@NoArgsConstructor
public class CompoundPackageAttachment<P extends LocalPackage> extends PackageAttachment<P> {

    /**
     * The list of package attachments to execute.
     */
    protected List<PackageAttachment<P>> list;

    @Override
    public void install(PackageManager packageManager, ManagedNode node, Path nodePath, P localPackage) {
        for (PackageAttachment<P> attachment : list) {
            attachment.install(packageManager, node, nodePath, localPackage);
        }
    }

}
