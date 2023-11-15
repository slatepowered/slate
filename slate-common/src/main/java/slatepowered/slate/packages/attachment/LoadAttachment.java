package slatepowered.slate.packages.attachment;

import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.plugin.SlatePlugin;
import slatepowered.slate.plugin.SlatePluginManager;
import slatepowered.veru.reflect.Classloading;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the given JAR files from the package into the system class loader.
 */
public class LoadAttachment<P extends LocalPackage> extends PackageAttachment<P> {

    /**
     * The list of files to load.
     */
    private List<String> files;

    /**
     * Whether it should load any plugins included in the files.
     */
    private boolean loadPlugins;

    public LoadAttachment(PackageKey<P> fromPackage, List<String> files, boolean loadPlugins) {
        super(fromPackage);
        this.files = files;
        this.loadPlugins = loadPlugins;
    }

    public LoadAttachment() {

    }

    @Override
    public void install(PackageManager packageManager, ManagedNode node, Path nodePath, P localPackage) {
        if (node.getNetwork().local() != node) {
            throw new IllegalArgumentException("Can not immediately load library for a remote node");
        }

        try {
            List<URL> urls = new ArrayList<>();
            Path packagePath = localPackage.getPath();
            for (String fileStr : files) {
                Path filePath = packagePath.resolve(fileStr);
                if (!Files.exists(filePath) || Files.isDirectory(filePath))
                    continue;

                // try and load file path as plugin
                if (loadPlugins) {
                    final SlatePluginManager pluginManager = node.getNetwork().getPluginManager();
                    final SlatePlugin plugin = pluginManager.constructFromFile(filePath);
                    if (plugin != null) {
                        pluginManager.load(plugin);
                        // dont register to classloading, as it should've been
                        // loaded by now
                        continue;
                    }
                }

                // register URL
                urls.add(filePath.toUri().toURL());
            }

            // add all URLs to classloading
            Classloading.addURLs(ClassLoader.getSystemClassLoader(), urls.toArray(new URL[0]));
        } catch (Throwable t) {
            throw new RuntimeException("Failed to load libraries immediately", t);
        }
    }

}
