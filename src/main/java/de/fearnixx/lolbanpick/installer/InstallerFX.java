package de.fearnixx.lolbanpick.installer;


import de.fearnixx.lolbanpick.ShutdownListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InstallerFX implements ShutdownListener {

    @FXML
    private Pane stepsContainer;

    @FXML
    private Button startInstallerBtn;

    @FXML
    private ProgressBar progress;

    private boolean started = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onShutdown() {
        executorService.shutdownNow();
    }

    public void startInstaller() {
        if (started) {
            return;
        }
        started = true;
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
    }

    private void addStep(String text) {
        stepsContainer.getChildren().add(new Text(text));
    }
}
