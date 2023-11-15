package slatepowered.slate.plugin;

import slatepowered.slate.model.Network;

public interface PluginEntrypoint<N extends Network> {

    /**
     * Called when this plugin is loaded.
     *
     * @param plugin The plugin.
     * @param network The local network it was loaded for.
     */
    default void onLoad(SlatePlugin plugin, N network) {

    }

    /**
     * Called when the network and this plugin should be
     * initialized.
     *
     * @param plugin The plugin.
     * @param network The network.
     */
    default void onInitialize(SlatePlugin plugin, N network) {

    }

    /**
     * Called when this plugin is disabled/destroyed.
     *
     * @param plugin The plugin.
     * @param network The network.
     */
    default void onDisable(SlatePlugin plugin, N network) {

    }

}
