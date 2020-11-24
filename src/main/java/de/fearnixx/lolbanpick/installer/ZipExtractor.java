package de.fearnixx.lolbanpick.installer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {

    public static void extractTo(Path zipFile, Path targetContainer) throws IOException {
        try (var input = new ZipInputStream(Files.newInputStream(zipFile, StandardOpenOption.READ))) {
            ZipEntry e;
            while ((e = input.getNextEntry()) != null) {
                if (e.isDirectory()) {
                    Files.createDirectories(targetContainer.resolve(e.getName()));
                } else {
                    Files.copy(input, targetContainer.resolve(e.getName()));
                }
            }
        }
    }
}
