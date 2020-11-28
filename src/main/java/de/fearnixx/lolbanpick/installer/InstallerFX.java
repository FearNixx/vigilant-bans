package de.fearnixx.lolbanpick.installer;


import de.fearnixx.lolbanpick.Constants;
import de.fearnixx.lolbanpick.HostServicesAware;
import de.fearnixx.lolbanpick.ShutdownListener;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class InstallerFX implements ShutdownListener, HostServicesAware {

    @FXML
    private Pane stepsContainer;

    @FXML
    private Button startInstallerBtn;

    @FXML
    private ProgressBar progress;

    private final AtomicBoolean started = new AtomicBoolean();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final List<Runnable> onDoneListeners = new LinkedList<>();
    private final AtomicReference<HostServices> hostServices = new AtomicReference<>();

    @Override
    public void onShutdown() {
        executorService.shutdownNow();
    }

    @Override
    public void setHostServices(HostServices hostServices) {
        this.hostServices.set(hostServices);
    }

    public synchronized void onDone(Runnable listener) {
        onDoneListeners.add(listener);
    }

    public void startInstaller() {
        synchronized (this) {
            if (started.get()) {
                return;
            }
            started.set(true);
        }
        startInstallerBtn.setDisable(true);
        progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progress.setVisible(true);
        addStep("Starting installation in background.");
        final var worker = new InstallerWorker();
        worker.onStep(txt -> Platform.runLater(() -> addStep(txt)));
        worker.onFinish(() -> Platform.runLater(this::finishedInstaller));
        executorService.execute(worker);
    }

    private void finishedInstaller() {
        addStep("Finished!");
        progress.setProgress(100D);
        synchronized (this) {
            started.set(false);
            onDoneListeners.forEach(Runnable::run);
            stepsContainer.getChildren().clear();
        }
    }

    private void addStep(String text) {
        stepsContainer.getChildren().add(new Text(text));
    }

    public void openSocialTwitter() {
        hostServices.get().showDocument(Constants.SOCIAL_TWITTER);
    }

    public void openSocialGitHub() {
        hostServices.get().showDocument(Constants.SOCIAL_GITHUB);
    }
}
