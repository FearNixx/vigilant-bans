package de.fearnixx.lolbanpick.runner;

import de.fearnixx.lolbanpick.Constants;
import de.fearnixx.lolbanpick.ManagerFX;
import de.fearnixx.lolbanpick.ShutdownListener;
import de.fearnixx.lolbanpick.config.ConfigFX;
import de.fearnixx.lolbanpick.fs.DeletingFileVisitor;
import de.fearnixx.lolbanpick.proc.nodejs.NPMProcessRunner;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class RunnerFX implements ShutdownListener, Initializable {

    private static final Logger logger = LoggerFactory.getLogger(RunnerFX.class);
    private static final ExecutorService childExecutor = Executors.newFixedThreadPool(3, task -> {
        var t = new Thread(task);
        t.setPriority(Thread.MAX_PRIORITY - 1);
        return t;
    });
    private final NPMProcessRunner lcuBroker = new NPMProcessRunner("run", "start");
    private final NPMProcessRunner layoutServer = new NPMProcessRunner("run", "start");
    private final AtomicReference<Stage> configStage = new AtomicReference<>();
    private final AtomicReference<ConfigFX> configController = new AtomicReference<>();

    @FXML
    private AnchorPane main;

    @FXML
    private Button lcuBrokerStart;

    @FXML
    private Button lcuBrokerStop;

    @FXML
    private Button layoutServerStart;

    @FXML
    private Button layoutServerStop;

    @FXML
    private Button configOpen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lcuBroker.setWorkingDirectory(Constants.PICKBAN_DIR.toPath());
        lcuBroker.onDone(() -> Platform.runLater(this::onLCUBrokerDone));
        layoutServer.setWorkingDirectory(Constants.PICKBAN_EULAYOUT_DIR.toPath());
        layoutServer.onDone(() -> Platform.runLater(this::onEULayoutServerDone));
    }

    @Override
    public void onShutdown() {
        childExecutor.shutdownNow();
        synchronized (configStage) {
            if (configController.get() != null) {
                configController.get().onShutdown();
            }
            if (configStage.get() != null) {
                configStage.get().close();
            }
        }
    }

    public void runLCUBroker() {
        if (!lcuBroker.isAlive()) {
            lcuBrokerStart.setDisable(true);
            lcuBrokerStop.setVisible(true);
            childExecutor.execute(lcuBroker);
        }
    }

    private void onLCUBrokerDone() {
        lcuBrokerStart.setDisable(false);
        lcuBrokerStop.setVisible(false);
    }

    public void stopLCUBroker() {
        if (lcuBroker.isAlive()) {
            lcuBroker.stop();
        }
    }

    public void runEULayoutServer() {
        if (!layoutServer.isAlive()) {
            layoutServerStart.setDisable(true);
            layoutServerStop.setVisible(true);
            childExecutor.execute(layoutServer);
        }
    }

    private void onEULayoutServerDone() {
        layoutServerStart.setDisable(false);
        layoutServerStop.setVisible(false);
    }

    public void stopEULayoutServer() {
        if (layoutServer.isAlive()) {
            layoutServer.stop();
        }
    }

    public void openInstaller() {
        main.setDisable(true);
        childExecutor.execute(() -> {
            try {
                Files.walkFileTree(Constants.appsDir.toPath(), new DeletingFileVisitor());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void openConfig() {
        synchronized (configStage) {
            if (configStage.get() != null) {
                configStage.get().show();
                return;
            }

            configOpen.setDisable(true);
            try {
                final var configFXMLLoader = new FXMLLoader(ManagerFX.findResource("config.fxml"));
                final Parent root = configFXMLLoader.load();
                final var stage = new Stage();
                stage.setTitle(String.format(ManagerFX.APP_TITLE_FORMAT, "Config"));
                stage.setScene(new Scene(root, 600, 400));
                stage.setOnHidden(e -> {
                    configStage.set(null);
                    configController.get().onShutdown();
                    configController.set(null);
                    configOpen.setDisable(false);
                });
                stage.show();

                configController.set(configFXMLLoader.getController());
                configStage.set(stage);
            } catch (IOException e) {
                logger.error("Error opening config stage!", e);
            }
        }
    }
}
