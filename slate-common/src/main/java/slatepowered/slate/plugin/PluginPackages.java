package slatepowered.slate.plugin;

import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.local.LocalFilesPackage;

public final class PluginPackages {

    /**
     * Get a named package key for the given plugin.
     *
     * @param plugin The plugin.
     * @return The package key.
     */
    public static PackageKey<LocalFilesPackage> forPlugin(SlatePlugin plugin) {
        return PackageKey.named(plugin.getId());
    }

}
