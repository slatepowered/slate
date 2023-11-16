package slatepowered.slate.packages.attachment;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import slatepowered.slate.model.ManagedNode;
import slatepowered.slate.packages.LocalPackage;
import slatepowered.slate.packages.PackageAttachment;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.veru.data.Pair;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

// Links files from the installed package to a destination
// in the node directory
public class LinkMatchingFiles<P extends LocalPackage> extends PackageAttachment<P> {

    /**
     * Resolves the source path to a destination path, skipping
     * it if null is returned.
     */
    protected BiFunction<Path, Path, Path> resolver;

    public LinkMatchingFiles() {

    }

    public LinkMatchingFiles(PackageKey<P> fromPackage, BiFunction<Path, Path, Path> resolver) {
        super(fromPackage);
        this.resolver = resolver;
    }

    @Override
    protected void install0(PackageManager packageManager, ManagedNode node, Path nodePath, P localPackage) {
        try {
            Path sources = localPackage.getPath();
            Files.walk(sources)
                    .map(p -> Pair.of(p, resolver.apply(nodePath, sources.relativize(p))))
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