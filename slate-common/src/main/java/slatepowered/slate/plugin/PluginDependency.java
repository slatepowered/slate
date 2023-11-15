package slatepowered.slate.plugin;

/**
 * Represents the dependency of a plugin on another package/resource/plugin.
 */
public interface PluginDependency {

    /**
     * Ensure this dependency is loaded and otherwise load it.
     *
     * @param plugin The plugin this dependency is for.
     * @param pluginManager The plugin manager.
     */
    void ensureLoaded(SlatePlugin plugin, SlatePluginManager pluginManager) throws Throwable;

    static PluginDependency otherPlugin(final String id) {
        return new PluginDependency() {
            @Override
            public void ensureLoaded(SlatePlugin plugin, SlatePluginManager pluginManager) throws Throwable {
                // get plugin object from the manager
                SlatePlugin otherPlugin = pluginManager.getPlugin(id);
                if (otherPlugin == null) {
                    throw new PluginDependencyException("Plugin by id `" + id + "` was not found, which is required by " + plugin);
                }

                // load plugin
                pluginManager.load(otherPlugin);
            }
        };
    }

}
