package slatepowered.slate.plugin;

import java.nio.file.Path;
import java.util.List;

/**
 * A plugin which was loaded from a file.
 */
public final class FilePlugin extends SlatePlugin {

    /**
     * The source file which contains this plugin.
     */
    protected final Path path;

    public FilePlugin(SlatePluginManager manager, String id, String name, String version, List<PluginDependency> dependencies, List<CompiledPluginEntrypoint> entrypoints,
                      Path path) {
        super(manager, id, name, version, dependencies, entrypoints);
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "file " + super.toString();
    }

}
