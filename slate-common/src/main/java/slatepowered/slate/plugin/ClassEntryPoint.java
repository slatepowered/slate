package slatepowered.slate.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import slatepowered.slate.model.Network;

import java.lang.reflect.Constructor;

/**
 * Represents a simple entry point which loads a class extending
 * {@link PluginEntrypoint} and executes the appropriate methods when
 * valid.
 */
@RequiredArgsConstructor
@Getter
final class ClassEntryPoint implements CompiledPluginEntrypoint {

    /**
     * The name of the class to load.
     */
    protected final String className;

    /**
     * The loaded entrypoint class.
     */
    protected transient Class<?> loadedClass;

    /**
     * The created entrypoint instance.
     */
    protected transient PluginEntrypoint<Network> instance;

    @Override
    public boolean isValid(SlatePlugin plugin, SlatePluginManager manager) throws Throwable {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadAndExecute(SlatePlugin plugin, SlatePluginManager manager) throws Throwable {
        // load the entrypoint class
        loadedClass = Class.forName(className);
        if (!PluginEntrypoint.class.isAssignableFrom(loadedClass)) {
            throw new IllegalArgumentException("Class " + loadedClass.getName() + " set as entrypoint of " + plugin + " does not implement PluginEntrypoint");
        }

        // create instance
        Constructor<?> constructor = loadedClass.getConstructor();
        instance = (PluginEntrypoint<Network>) constructor.newInstance();

        // call onLoad()
        instance.onLoad(plugin, manager.getNetwork());

        // register events
        plugin.onInitialize.then(__ -> instance.onInitialize(plugin, manager.getNetwork()));
        plugin.onDestroy.then(__ -> instance.onDisable(plugin, manager.getNetwork()));
    }

}
