package slatepowered.slate.plugin;

import slatepowered.slate.model.Network;

public interface PluginEntrypoint<N extends Network> {

    /**
     * Called when this plugin is loaded.
     *
     * @param plugin The plugin.
     */
    default void onLoad(SlatePlugin plugin) {

    }

    /**
     * Called when the network and this plugin should be initialized
     * on the given network.
     *
     * @param plugin The plugin.
     * @param network The network.
     */
    default void onInitialize(SlatePlugin plugin, N network) {

    }

    /**
     * Called when this plugin is disabled/destroyed for the given network.
     *
     * @param plugin The plugin.
     * @param network The network.
     */
    default void onDisable(SlatePlugin plugin, N network) {

    }

    /**
     * Called when the plugin manager destroys all plugins.
     *
     * @param plugin The plugin.
     */
    default void onDestroy(SlatePlugin plugin) {

    }

}
