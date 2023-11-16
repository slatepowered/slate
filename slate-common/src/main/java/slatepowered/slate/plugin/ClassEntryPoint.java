package slatepowered.slate.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import slatepowered.slate.model.Network;
import slatepowered.veru.reflect.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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

    /**
     * The network class defined when implementing PluginEntrypoint.
     */
    protected transient Class<? extends Network> networkClass;

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

        // find the network type
        for (Type type : loadedClass.getGenericInterfaces()) {
            if (type.getTypeName().equals(PluginEntrypoint.class.getTypeName())) {
                if (type instanceof ParameterizedType) {
                    networkClass = (Class<? extends Network>) ReflectUtil.getClassForType(((ParameterizedType) type).getActualTypeArguments()[0]);
                } else {
                    // no type parameter found, use plain Network class
                    networkClass = Network.class;
                }
            }
        }

        // create instance
        Constructor<?> constructor = loadedClass.getConstructor();
        instance = (PluginEntrypoint<Network>) constructor.newInstance();

        // call onLoad()
        instance.onLoad(plugin);

        // register events
        plugin.onInitialize.then(net -> { if (!networkClass.isAssignableFrom(net.getClass()))
            instance.onInitialize(plugin, net); });
        plugin.onDisable.then(net -> { if (!networkClass.isAssignableFrom(net.getClass()))
            instance.onDisable(plugin, net); });
    }

}
