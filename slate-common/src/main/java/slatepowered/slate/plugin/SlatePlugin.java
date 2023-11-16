package slatepowered.slate.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import slatepowered.slate.model.Network;
import slatepowered.veru.functional.Callback;

import java.util.List;

/**
 * A plugin is registered locally to a network and can
 * hook into different aspects of that network.
 *
 * This can be dynamically loaded from packages as well.
 */
@RequiredArgsConstructor
@Getter
public class SlatePlugin {

    /**
     * The plugin manager.
     */
    protected final SlatePluginManager manager;

    /**
     * The ID of this plugin.
     */
    protected final String id;

    /**
     * The name of the plugin.
     */
    protected final String name;

    /**
     * The version of the plugin.
     */
    protected final String version;

    /**
     * The dependencies of this plugin.
     */
    protected final List<PluginDependency> dependencies;

    /**
     * All parsed entry points of this plugin.
     */
    protected final List<CompiledPluginEntrypoint> entrypoints;

    /**
     * All loaded entry points.
     */
    protected List<CompiledPluginEntrypoint> loadedEntrypoints;

    /**
     * Whether this plugin has successfully been loaded and
     * is now technically available.
     */
    protected volatile boolean isLoaded;

    /**
     * Event: Called when this plugin should be initialized
     */
    public final Callback<Network> onInitialize = Callback.multi();

    /**
     * Event: Called when this plugin is disabled
     */
    public final Callback<Network> onDisable = Callback.multi();

    /**
     * Event: Called when this plugin is destroyed
     */
    public final Callback<Void> onDestroy = Callback.multi();

    @Override
    public String toString() {
        return "plugin(id: " + id + " version: " + version + (isLoaded ? " loaded" : "") + ")";
    }

}
