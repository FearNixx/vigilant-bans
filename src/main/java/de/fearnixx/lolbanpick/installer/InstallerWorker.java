package de.fearnixx.lolbanpick.installer;

import de.fearnixx.lolbanpick.Constants;
import de.fearnixx.lolbanpick.fs.DeletingFileVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InstallerWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(InstallerWorker.class);
    private final List<Consumer<String>> stepListeners = new LinkedList<>();
    private final List<Runnable> doneListeners = new LinkedList<>();

    public synchronized void onStep(Consumer<String> listener) {
        stepListeners.add(listener);
    }

    public synchronized void onFinish(Runnable listener) {
        doneListeners.add(listener);
    }

    private void echoStep(String text) {
        logger.info("Step: {}", text);
        stepListeners.forEach(l -> l.accept(text));
    }

    @Override
    public void run() {
        final List<Runnable> steps = List.of(
                this::clearOutput,
                () -> downloadNodeJS().ifPresent(this::extractNodeJS),
//                () -> downloadGitWin().ifPresent(this::extractGitWin),
                () -> downloadRepository().ifPresent(this::extractRepository),
                this::installNodeDependencies,
                this::notifyDone
        );
        steps.stream()
                .filter(s -> !Thread.currentThread().isInterrupted())
                .forEach(Runnable::run);
        if (Thread.currentThread().isInterrupted()) {
            echoStep("Installation cancelled.");
            notifyDone();
        }
    }

    private void clearOutput() {
        if (Files.exists(Constants.appsDir.toPath())) {
            echoStep("Deleting existing dependencies -> Fresh install.");
            try {
                Files.walkFileTree(Constants.appsDir.toPath(), new DeletingFileVisitor());
            } catch (IOException e) {
                logger.error("Error deleting apps!", e);
                echoStep(String.format("Failed to delete files. (Msg: %s)", e.getLocalizedMessage()));
                Thread.currentThread().interrupt();
            }
        }
    }

    private Optional<Path> downloadNodeJS() {
        echoStep(String.format("Downloading NodeJS version %s (portable).", Constants.NODE_VERSION));
        try {
            return Optional.ofNullable(FileDownloader.downloadFile(Constants.NODE_URI, "nodejs.zip"));
        } catch (IOException e) {
            logger.error("Error downloading NodeJS!", e);
            echoStep(String.format("Failed to download NodeJS. (Msg: %s)", e.getLocalizedMessage()));
            return Optional.empty();
        }
    }

    private void extractNodeJS(Path archivePath) {
        echoStep("Extracting NodeJS.");
        try {
            ZipExtractor.extractTo(archivePath, Constants.appsDir.toPath());
        } catch (IOException e) {
            logger.error("Failed to extract NodeJS!", e);
            echoStep(String.format("Failed to extract NodeJS. (Msg: %s)", e.getLocalizedMessage()));
        }
    }

    private Optional<Path> downloadGitWin() {
        echoStep(String.format("Downloading Git-For-Windows %s (portable).", Constants.GITWIN_VERSION));
        try {
            return Optional.ofNullable(FileDownloader.downloadFile(Constants.GITWIN_URI, "git-for-windows-portable.exe"));
        } catch (IOException e) {
            logger.error("Error downloading Git-For-Windows!", e);
            echoStep(String.format("Failed to download Git-For-Windows. (Msg: %s)", e.getLocalizedMessage()));
            return Optional.empty();
        }
    }

    private void extractGitWin(Path exeFile) {
        echoStep("Extracting Git-For-Windows.");
        final var targetDirectoryArg = String.format("\"%s\"", Constants.GITWIN_DIR);
        Process process = null;
        try {
            final var processBuilder = new ProcessBuilder()
                    .command(exeFile.toString(), "-o", targetDirectoryArg, "-y")
                    .inheritIO();
            processBuilder.environment().put(Constants.OS_PATH_ENV, String.format("\"%s\"%s%s", Constants.NODE_DIR, File.pathSeparator, System.getenv(Constants.OS_PATH_ENV)));
            process = processBuilder.start();
            process.waitFor();
        } catch (IOException e) {
            logger.error("Error starting git extraction!", e);
            echoStep(String.format("Failed to extract Git-For-Windows. (Msg: %s)", e.getLocalizedMessage()));
        } catch (InterruptedException e) {
            logger.warn("Interrupted -> Killing child.");
            process.destroy();
            Thread.currentThread().interrupt();
        }
    }

    private Optional<Path> downloadRepository() {
        echoStep("Downloading LoL-Ban-Pick-UI-Repository.");
        try {
            return Optional.ofNullable(FileDownloader.downloadFile(Constants.PICKBAN_ARCHIVE, "pickban-ui.zip"));
        } catch (IOException e) {
            logger.error("Error downloading Git archive!", e);
            echoStep(String.format("Failed to download Git archive. (Msg: %s)", e.getLocalizedMessage()));
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private void extractRepository(Path zipArchive) {
        try {
            ZipExtractor.extractTo(zipArchive, Constants.appsDir.toPath());
        } catch (IOException e) {
            logger.error("Error extracting Git archive!", e);
            echoStep(String.format("Failed to extract Git archive. (Msg: %s)", e.getLocalizedMessage()));
            Thread.currentThread().interrupt();
        }
    }

    private void activateYarn() {
        Process proc = null;
        try {
            // === BEGIN LCU Broker dependencies ===
            echoStep("Enabling Yarn for JS dependency management");
            final var processBuilder = new ProcessBuilder()
                    .command(Constants.NPM_EXECUTABLE.toPath().toString(), "corepack enable")
                    .directory(Constants.PICKBAN_DIR)
                    .inheritIO();
            processBuilder.environment().put(Constants.OS_PATH_ENV, String.format("\"%s\"%s%s", Constants.NODE_DIR, File.pathSeparator, System.getenv(Constants.OS_PATH_ENV)));
            proc = processBuilder.start();
            proc.waitFor();
        } catch (IOException e) {
            logger.error("Error enabling corepack!", e);
            echoStep(String.format("Failed to enable yarn! (Msg: %s)", e.getLocalizedMessage()));
            Thread.currentThread().interrupt();
            return;
        } catch (InterruptedException e) {
            logger.warn("Corepack enabling interrupted!");
            proc.destroy();
            Thread.currentThread().interrupt();
            return;
        }
    }

    private void installNodeDependencies() {
        Process proc = null;
        try {
            // === BEGIN LCU Broker dependencies ===
            echoStep("Installing JS dependencies (LCU-Broker).");
            final var processBuilder = new ProcessBuilder()
                    .command(Constants.NPM_EXECUTABLE.toPath().toString(), "install")
                    .directory(Constants.PICKBAN_DIR)
                    .inheritIO();
            processBuilder.environment().put(Constants.OS_PATH_ENV, String.format("\"%s\"%s%s", Constants.NODE_DIR, File.pathSeparator, System.getenv(Constants.OS_PATH_ENV)));
            proc = processBuilder.start();
            proc.waitFor();
        } catch (IOException e) {
            logger.error("Error installing JS dependencies for LCU-Broker!", e);
            echoStep(String.format("Failed to install LCU-Broker dependencies! (Msg: %s)", e.getLocalizedMessage()));
            Thread.currentThread().interrupt();
            return;
        } catch (InterruptedException e) {
            logger.warn("NPM Install interrupted!");
            proc.destroy();
            Thread.currentThread().interrupt();
            return;
        }

        try {
            // === BEGIN layout dependencies ===
            echoStep("Installing JS dependencies (EU-Layout).");
            final var processBuilder = new ProcessBuilder()
                    .command(Constants.NPM_EXECUTABLE.toPath().toString(), "install")
                    .directory(Constants.PICKBAN_EULAYOUT_DIR)
                    .inheritIO();
            processBuilder.environment().put(Constants.OS_PATH_ENV, String.format("\"%s\"%s%s", Constants.NODE_DIR, File.pathSeparator, System.getenv(Constants.OS_PATH_ENV)));
            proc = processBuilder.start();
            proc.waitFor();
        } catch (IOException e) {
            logger.error("Error installing JS dependencies for EU layout!", e);
            echoStep(String.format("Failed to install eu-layout dependencies! (Msg: %s)", e.getLocalizedMessage()));
            Thread.currentThread().interrupt();
            return;
        } catch (InterruptedException e) {
            logger.warn("NPM Install interrupted!");
            proc.destroy();
            Thread.currentThread().interrupt();
            return;
        }

        echoStep("JS dependencies installed successfully.");
    }

    private synchronized void notifyDone() {
        doneListeners.forEach(Runnable::run);
    }

    public static boolean checkPreconditions() {
        final var files = Constants.appsDir.listFiles();
        if (files == null) {
            return false;
        }

        final var directories = Arrays.stream(files)
                .filter(File::isDirectory)
                .collect(Collectors.toSet());
        return directories.stream().anyMatch(f -> f.getName().contains(Constants.NODE_DIR.getName()))
//                && directories.stream().anyMatch(f -> f.getName().contains(Constants.GITWIN_DIR.getName()))
                && directories.stream().anyMatch(f -> f.getName().contains(Constants.REPO_DIR.getName()));
    }
}
