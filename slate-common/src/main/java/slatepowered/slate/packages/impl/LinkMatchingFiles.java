package slatepowered.slate.packages.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageManager;
import slatepowered.veru.data.Pair;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

// Links files from the installed package to a destination
// in the node directory
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class LinkMatchingFiles extends PackageAttachment {

    /**
     * Resolves the source path to a destination path, skipping
     * it if null is returned.
     */
    protected Function<Path, Path> resolver;

    @Override
    public void install(PackageManager packageManager, ManagedNode node, Path nodePath, LocalPackage localPackage) {
        try {
            Path sources = localPackage.getPath();
            Files.walk(sources)
                    .map(p -> Pair.of(p, resolver.apply(p)))
                    .filter(p -> p.getSecond() != null)
                    .forEach(pathPair -> {
                        Path src = pathPair.getFirst();
                        Path dst = pathPair.getSecond();

                        try {
                            if (Files.isDirectory(src)) {
                                // create symbolic link for directories
                                Files.createSymbolicLink(dst, src);
                            } else {
                                // check whether the destination path was
                                // already symbolically linked (check whether
                                // it is a real path)
                                if (dst.toRealPath().equals(dst)) {
                                    Files.createLink(dst, src);
                                }
                            }
                        } catch (Throwable t) {
                            Throwables.sneakyThrow(t);
                        }
                    });
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }
    }

}