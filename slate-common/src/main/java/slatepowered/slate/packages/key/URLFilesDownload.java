package slatepowered.slate.packages.key;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.local.LocalFilesPackage;
import slatepowered.veru.data.Pair;
import slatepowered.veru.io.IOUtil;

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
            for (Pair<String, String> pair : files) {
                String url = pair.getFirst();
                String filename = pair.getSecond();

                Path outputFile = path.resolve(filename);
                IOUtil.download(IOUtil.parseURL(url), outputFile);
            }

            return new LocalFilesPackage(manager, this, path);
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
