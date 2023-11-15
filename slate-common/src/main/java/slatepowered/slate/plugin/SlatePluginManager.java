package slatepowered.slate.plugin;

import com.eclipsesource.json.*;
import lombok.RequiredArgsConstructor;
import slatepowered.slate.model.Network;
import slatepowered.veru.io.IOUtil;
import slatepowered.veru.misc.Throwables;
import slatepowered.veru.reflect.Classloading;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Dynamically loads/manages {@link SlatePlugin} instances.
 */
@RequiredArgsConstructor
public abstract class SlatePluginManager {

    /**
     * The resource to find the plugin configuration at.
     */
    private static final String pluginDataResource = "slate.plugin.json";

    /**
     * The network this plugin manager was created for.
     */
    protected final Network network;

    /**
     * The list of all plugins loaded by this plugin manager.
     */
    protected final Map<String, SlatePlugin> plugins = new HashMap<>();

    /**
     * Get the name of the local environment.
     *
     * @return The environment.
     */
    public abstract String[] getEnvironmentNames();

    public Network getNetwork() {
        return network;
    }

    public Map<String, SlatePlugin> getPlugins() {
        return plugins;
    }

    /**
     * Try and get a plugin for the given ID.
     *
     * @param id The ID.
     * @return The plugin or null if absent.
     */
    public SlatePlugin getPlugin(String id) {
        return plugins.get(id);
    }

    // get the variable for the given key
    // in this environment
    protected Object getVariable(SlatePlugin plugin, String key) {
        switch (key) {
            case "environment": case "env": return getEnvironmentNames();
        }

        return null;
    }

    // get a URL for a resource by the given name
    // in the given JAR file
    private URL getResource(Path path, String name) {
        try {
            return new URL("jar:file:" + path.toAbsolutePath() + "!/" + name);
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
            throw new AssertionError();
        }
    }

    /**
     * Parses a plugin dependency from the given JSON pair.
     *
     * @param key The key of the pair.
     * @param value The value of the pair.
     * @return The dependency.
     */
    public PluginDependency parseDependency(String key, JsonValue value) {
        String key1 = key.split(" ")[0];

        // check for other plugin
        if ("plugin".equals(key1)) {
            String otherPluginId = value.asString();
            return PluginDependency.otherPlugin(otherPluginId);
        }

        // check for URL package

        return null;
    }

    enum CompareMode {
        /**
         * The compare value is any of the elements in the array.
         */
        ANY_OF,

        /**
         * Is A equal to B.
         */
        EQ
    }

    // compare the given json value to the given
    // compare value in the given compare mode
    private boolean compareValue(Object actual, Object compareValue, CompareMode mode) {
        if (actual == compareValue || actual == null || compareValue == null) return true;

        if (Objects.equals(actual, compareValue)) return true;

        if (actual.getClass().isArray()) {
            Object[] actualArr = (Object[]) actual;

            // todo: other compare modes
            if (mode == CompareMode.ANY_OF) {
                for (Object elem : actualArr) {
                    if (compareValue(actual, compareValue, mode)) {
                        return true;
                    }
                }
            }
        }

        // todo: other shit
        return false;
    }

    // convert a given json value to a java value
    private Object jsonValueToValue(JsonValue value) {
        if (value == null || value.isNull()) return null;

        if (value.isObject()) {
            return value.asObject();
        } else if (value.isArray()) {
            // convert all elements
            JsonArray jsonArray = value.asArray();
            final int length = jsonArray.size();
            Object[] arr = new Object[length];
            for (int i = 0; i < length; i++) {
                arr[i] = jsonValueToValue(jsonArray.get(i));
            }

            return arr;
        } else if (value.isNumber()) {
            return value.asDouble();
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isString()) {
            return value.asString();
        }

        throw new IllegalArgumentException("Weird JSON value " + value);
    }

    /**
     * Parse an entrypoint from the given JSON object.
     *
     * @param object The JSON object.
     * @return The parsed entrypoint.
     */
    public CompiledPluginEntrypoint parseEntrypoint(JsonObject object) {
        // parse any predicates
        List<Predicate<SlatePlugin>> predicates = new ArrayList<>();
        for (String key : object.names()) {
            if (key.startsWith("*")) {
                final Object compareValue = jsonValueToValue(object.get(key));
                final String varName = key.substring(1);

                predicates.add(plugin -> compareValue(getVariable(plugin, varName), compareValue, CompareMode.ANY_OF));
            }
        }

        if (!predicates.isEmpty()) {
            CompiledPluginEntrypoint entrypoint = parseEntrypoint(object);

            // wrap entrypoint in predicate
            return entrypoint.onlyIf(predicates);
        }

        // check for class entry point
        if (object.get("class") != null) {
            String className = object.getString("class", null);
            return new ClassEntryPoint(className);
        }

        return null;
    }

    /**
     * Constructs a plugin from the given file path.
     *
     * @param path The path.
     * @return The plugin instance.
     */
    public SlatePlugin constructFromFile(Path path) {
        try {
            // find plugin description
            URL pluginDescURL = getResource(path, pluginDataResource);
            InputStream stream = pluginDescURL.openStream();
            if (stream == null)
                return null;
            String strContent = new String(IOUtil.readAllBytes(stream), StandardCharsets.UTF_8);
            stream.close();

            // parse plugin description
            JsonObject pluginDesc = JsonObject.readFrom(strContent);

            final String pluginId = pluginDesc.getString("id", null);
            final String pluginName = pluginDesc.getString("name", null);
            final String pluginVersion = pluginDesc.getString("version", null);

            final JsonObject pluginDependenciesArray = pluginDesc.get("dependencies").asObject();
            final JsonArray pluginEntrypointArray = pluginDesc.get("entrypoints").asArray();

            // parse plugin dependencies
            final List<PluginDependency> dependencies = pluginDependenciesArray != null ? StreamSupport.stream(
                    Spliterators.spliterator(pluginDependenciesArray.iterator(), pluginDependenciesArray.size(), 0), false)
                    .map(m -> parseDependency(m.getName(), m.getValue()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()) : new ArrayList<>();

            // parse entry points
            final List<CompiledPluginEntrypoint> entrypoints = pluginEntrypointArray != null ? StreamSupport.stream(
                    Spliterators.spliterator(pluginEntrypointArray.iterator(), pluginEntrypointArray.size(), 0), false)
                    .map(v -> parseEntrypoint(v.asObject()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()) : new ArrayList<>();

            // load plugin JAR to the class loader
            Classloading.addURLs(ClassLoader.getSystemClassLoader(),
                    path.toUri().toURL());

            // create plugin descriptor
            return register(new SlatePlugin(this, pluginId, pluginName, pluginVersion, dependencies, entrypoints, network));
        } catch (Throwable t) {
            throw new RuntimeException("An error occurred while constructing plugin from path(" + path + ")", t);
        }
    }

    /**
     * Register and return the given plugin.
     *
     * @param plugin The plugin to register.
     * @return The registered plugin.
     */
    public SlatePlugin register(SlatePlugin plugin) {
        this.plugins.put(plugin.id, plugin);
        return plugin;
    }

    /**
     * Attempt to load the given plugin, this will load
     * it's dependencies and entry points and then execute
     * some kind of load event.
     *
     * @param plugin The plugin to load.
     */
    public void load(SlatePlugin plugin) {
        if (plugin.isLoaded())
            return;

        try {
            // ensure dependencies are loaded
            for (PluginDependency dependency : plugin.getDependencies()) {
                dependency.ensureLoaded(plugin, this);
            }

            // load entrypoints
            plugin.loadedEntrypoints = new ArrayList<>();
            for (CompiledPluginEntrypoint entrypoint : plugin.getEntrypoints()) {
                if (entrypoint.isValid(plugin, this)) {
                    try {
                        entrypoint.loadAndExecute(plugin, this);
                    } catch (Throwable t) {
                        throw new RuntimeException("Failed to load and execute entrypoint:" + entrypoint + " for plugin(id: " + plugin.getId() + " version: " + plugin.getVersion() + ")", t);
                    }
                }
            }

            // plugin was loaded successfully
            plugin.isLoaded = true;
        } catch (Throwable t) {
            throw new RuntimeException("An error occurred while loading plugin(id: " + plugin.getId() + " version: " + plugin.getVersion() + ")", t);
        }
    }

}
