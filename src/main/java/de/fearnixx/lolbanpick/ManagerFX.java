package de.fearnixx.lolbanpick;

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
import java.util.concurrent.atomic.AtomicReference;

public class ManagerFX extends Application {

    public static final String APP_TITLE_FORMAT = "Vigilant-Bans | %s";
    private static final Logger logger = LoggerFactory.getLogger(ManagerFX.class);
    private static final AtomicReference<ManagerFX> INSTANCE = new AtomicReference<>();

    private static final List<ShutdownListener> shutdownListeners = new LinkedList<>();

    public ManagerFX() {
        synchronized (INSTANCE) {
            if (INSTANCE.get() != null) {
                throw new IllegalStateException("Cannot start multiple instances!");
            }
            INSTANCE.set(this);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
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

    private void openInstaller(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(findResource("installer.fxml"));
        Parent root = loader.load();
        registerShutdownListener(loader.getController());
        primaryStage.setScene(new Scene(root, 800D, 600D));
        primaryStage.setTitle(String.format(APP_TITLE_FORMAT, "Installer"));
        primaryStage.setOnHidden(e -> performShutdown());
        primaryStage.show();
    }

    private void openRunner(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(findResource("runner.fxml"));
        Parent root  = loader.load();
        final RunnerFX runner = loader.getController();
        runner.setHostServices(getHostServices());
        registerShutdownListener(runner);
        primaryStage.setScene(new Scene(root, 600D, 200D));
        primaryStage.setTitle(String.format(APP_TITLE_FORMAT, "Runner"));
        primaryStage.setOnHidden(e -> performShutdown());
        primaryStage.show();
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

    public ManagerFX getInstance() {
        synchronized (INSTANCE) {
            return INSTANCE.get();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
