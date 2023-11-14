package slatepowered.slate.packages.key;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import slatepowered.slate.packages.PackageKey;
import slatepowered.slate.packages.PackageManager;
import slatepowered.slate.packages.local.LocalFilePackage;
import slatepowered.veru.io.IOUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A resolved package key which downloads a file.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class URLFileDownload extends ResolvedPackageKeyUnion<URLFileDownload, LocalFilePackage> {

    public static URLFileDownload download(String url) {
        return new URLFileDownload(url, "file");
    }

    /**
     * The URL string for the file to download.
     */
    protected String url;

    /**
     * The output file name.
     */
    protected String filename = "file";

    @Override
    public CompletableFuture<LocalFilePackage> installLocally(PackageManager manager, Path path) {
        return CompletableFuture.supplyAsync(() -> {
            Path outputFile = path.resolve(filename);
            IOUtil.download(IOUtil.parseURL(url), outputFile);
            return new LocalFilePackage(manager, this, path, outputFile);
        });
    }

    @Override
    public LocalFilePackage loadLocally(PackageManager manager, Path path) {
        if (!Files.exists(path))
            return null;
        return new LocalFilePackage(manager, this, path, path.resolve(filename));
    }

    @Override
    protected UUID makeUUID() {
        return PackageKey.stringToUUID(url);
    }

}
