package slatepowered.slate.model.action;

import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.model.SharedNodeComponent;
import slatepowered.slate.packages.PackageManager;

import java.nio.file.Path;

/**
 * Executes code on the node host at the time of node allocation.
 */
public interface NodeAllocationAdapter extends SharedNodeComponent {

    void initialize(PackageManager packageManager, ManagedNode node, Path nodePath);

}
