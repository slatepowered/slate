package slatepowered.slate.packages.attachment;

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

// Copies files from the installed package to a destination
// in the node directory
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CopyMatchingFiles<P extends LocalPackage> extends PackageAttachment<P> {

    /**
     * Resolves the source path to a destination path, skipping
     * it if null is returned.
     */
    protected Function<Path, Path> resolver;

    @Override
    public void install(PackageManager packageManager, ManagedNode node, Path nodePath, P localPackage) {
        try {
            Path sources = localPackage.getPath();
            Files.walk(sources)
                    .map(p -> Pair.of(p, resolver.apply(p)))
                    .filter(p -> p.getSecond() != null && !Files.isDirectory(p.getFirst()))
                    .forEach(pathPair -> {
                        Path src = pathPair.getFirst();
                        Path dst = pathPair.getSecond();

                        try {
                            Files.createDirectories(dst);
                            Files.copy(src, dst);
                        } catch (Throwable t) {
                            Throwables.sneakyThrow(t);
                        }
                    });
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
        }
    }

}
