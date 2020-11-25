package de.fearnixx.lolbanpick.installer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

public class FileDownloader {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloader.class);

    public static Path downloadFile(String uri, String downloadFileName) throws IOException {
        final var tempFile = Files.createTempDirectory("lolbanpickui-downloadcache").resolve(downloadFileName);
        logger.debug("Downloading to: {}", tempFile);
        final var request = HttpRequest.newBuilder(constantURI(uri))
                .timeout(Duration.ofMinutes(120))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            return getHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofFile(tempFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
            ).body();
        } catch (InterruptedException e) {
            logger.warn("Download interrupted!");
            Files.deleteIfExists(tempFile);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private static HttpClient getHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    private static URI constantURI(String theURI) {
        try {
            return new URI(theURI);
        } catch (URISyntaxException e) {
            logger.error("Constant URI is broken? How did you do this...", e);
            throw new RuntimeException(e);
        }
    }
}
