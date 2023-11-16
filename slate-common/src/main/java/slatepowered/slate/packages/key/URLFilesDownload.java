package slatepowered.slate.packages.key;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import slatepowered.slate.logging.Logger;
import slatepowered.slate.logging.Logging;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.local.LocalFilesPackage;
import slatepowered.veru.data.Pair;
import slatepowered.veru.io.IOUtil;
import slatepowered.veru.misc.Throwables;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A resolved package key which downloads a file.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class URLFilesDownload extends ResolvedPackageKeyUnion<URLFilesDownload, LocalFilesPackage> {

    private static final Logger LOGGER = Logging.getLogger("URLFilesDownload");

    public static URLFilesDownload download(String... arr) {
        if (arr.length % 2 != 0) {
            throw new IllegalArgumentException("Varargs is not even");
        }

        // parse varargs
        List<Pair<String, String>> files = new ArrayList<>();
        for (int i = 0; i < arr.length;) {
            String url = arr[i++];
            String filename = arr[i++];
            files.add(Pair.of(url, filename));
        }

        return new URLFilesDownload(files);
    }

    /**
     * The files to download, the first string is the
     * URL and the second is the output file name.
     */
    protected List<Pair<String, String>> files;

    @Override
    public CompletableFuture<LocalFilesPackage> installLocally(PackageManager manager, Path path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                for (Pair<String, String> pair : files) {
                    String url = pair.getFirst();
                    String filename = pair.getSecond();

                    Path outputFile = path.resolve(filename);
                    Files.createDirectories(outputFile.getParent());
                    LOGGER.debug("Downloading online content to file(" + outputFile + ") from url(" + url + ")");
                    IOUtil.download(IOUtil.parseURL(url), outputFile);
                }

                return new LocalFilesPackage(manager, this, path);
            } catch (Exception e) {
                Throwables.sneakyThrow(e);
                throw new AssertionError();
            }
        });
    }

    @Override
    public LocalFilesPackage loadLocally(PackageManager manager, Path path) {
        if (!Files.exists(path))
            return null;
        return new LocalFilesPackage(manager, this, path);
    }

    @Override
    protected UUID makeUUID() {
        return PackageKey.stringToUUID(Integer.toHexString(files.hashCode()));
    }

}
