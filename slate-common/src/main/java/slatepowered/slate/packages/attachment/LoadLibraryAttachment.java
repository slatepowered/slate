package slatepowered.slate.packages.attachment;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageManager;
import slatepowered.veru.reflect.Classloading;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the given JAR files from the package into the system class loader.
 */
@AllArgsConstructor
@NoArgsConstructor
public class LoadLibraryAttachment<P extends LocalPackage> extends PackageAttachment<P> {

    /**
     * The list of files to load.
     */
    private List<String> files;

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

                urls.add(filePath.toUri().toURL());
            }

            // add all URLs
            Classloading.addURLs(ClassLoader.getSystemClassLoader(), urls.toArray(new URL[0]));
        } catch (Throwable t) {
            throw new RuntimeException("Failed to load libraries immediately", t);
        }
    }

}
