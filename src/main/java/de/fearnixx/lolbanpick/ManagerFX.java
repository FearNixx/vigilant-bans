package de.fearnixx.lolbanpick;

import de.fearnixx.lolbanpick.installer.InstallerFX;
import de.fearnixx.lolbanpick.installer.InstallerWorker;
import de.fearnixx.lolbanpick.runner.RunnerFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ManagerFX extends Application {

    public static final String APP_TITLE_FORMAT = "Vigilant-Bans | %s";
    private static final Logger logger = LoggerFactory.getLogger(ManagerFX.class);

    private static final List<ShutdownListener> shutdownListeners = new LinkedList<>();

    @Override
    public void start(Stage primaryStage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        logger.info("Launching in Java {} and JavaFX {}", javaVersion, javafxVersion);

        Runtime.getRuntime().addShutdownHook(new Thread(ManagerFX::notifyShutdownHandlers));

        if (shouldStartInstaller()) {
            openInstaller(primaryStage);
        } else {
            openRunner(primaryStage);
        }
    }

    private boolean shouldStartInstaller() {
        return !InstallerWorker.checkPreconditions();
    }

    private void openInstaller(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(findResource("installer.fxml"));
            Parent root = loader.load();
            initController(loader.getController());
            primaryStage.setScene(new Scene(root, 800D, 600D));
            primaryStage.setTitle(String.format(APP_TITLE_FORMAT, "Installer"));
            primaryStage.setOnHidden(e -> performShutdown());

            loader.<InstallerFX>getController().onDone(() -> Platform.runLater(() -> {
                unregisterShutdownListener(loader.getController());
                openRunner(primaryStage);
            }));
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Error opening installer in primary stage!", e);
        }
    }

    private void openRunner(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(findResource("runner.fxml"));
            Parent root = loader.load();
            initController(loader.getController());

            primaryStage.setScene(new Scene(root, 600D, 200D));
            primaryStage.setTitle(String.format(APP_TITLE_FORMAT, "Runner"));
            primaryStage.setOnHidden(e -> performShutdown());

            loader.<RunnerFX>getController().setOnRequestInstaller(() -> Platform.runLater(() -> {
                unregisterShutdownListener(loader.getController());
                openInstaller(primaryStage);
            }));
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Error opening runner in primary stage!", e);
        }
    }

    private void initController(Object controller) {
        if (controller instanceof ShutdownListener) {
            registerShutdownListener(((ShutdownListener) controller));
        }
        if (controller instanceof HostServicesAware) {
            ((HostServicesAware) controller).setHostServices(getHostServices());
        }
    }

    public static URL findResource(String resourcePath) {
        var resource = ManagerFX.class.getResource(resourcePath);
        if (resource == null) {
            logger.debug("Failed to find fxml through #getClass->getResource, attempting to use class loader...");
            resource = ManagerFX.class.getClassLoader().getResource(resourcePath);
        }
        return resource;
    }

    public static void registerShutdownListener(ShutdownListener listener) {
        synchronized (shutdownListeners) {
            shutdownListeners.add(listener);
        }
    }

    public static void unregisterShutdownListener(ShutdownListener listener) {
        synchronized (shutdownListeners) {
            // Remove ALL references, just in case it was registered multiple times.
            shutdownListeners.removeIf(l -> Objects.equals(l, listener));
        }
    }

    @SuppressWarnings({"java:S2274", "java:S2142"})
    private void performShutdown() {
        notifyShutdownHandlers();
        try {
            synchronized (this) {
                this.wait(2000);
            }
        } catch (InterruptedException e) {
            // Ignore: we'll exit below!
        }
        Platform.exit();
        System.exit(0);
    }

    private static void notifyShutdownHandlers() {
        synchronized (shutdownListeners) {
            shutdownListeners.forEach(ShutdownListener::onShutdown);
            shutdownListeners.clear();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
