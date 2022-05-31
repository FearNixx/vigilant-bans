package de.fearnixx.lolbanpick.stage;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

public class StageFX {

    @FXML
    private Button minimizeBtn;

    @FXML
    private Button closeBtn;

    private final AtomicReference<Double> xOffset = new AtomicReference<>();
    private final AtomicReference<Double> yOffset = new AtomicReference<>();

    protected Stage getStage() {
        return ((Stage) minimizeBtn.getScene().getWindow());
    }

    public void minimizeClick() {
        getStage().setIconified(true);
    }

    public void closeClick() {
        getStage().close();
    }

    public void onMousePressed(MouseEvent event) {
        xOffset.set(getStage().getX() - event.getScreenX());
        yOffset.set(getStage().getY() - event.getScreenY());
    }

    public void onMouseDragged(MouseEvent event) {
        getStage().setX(event.getScreenX() + xOffset.get());
        getStage().setY(event.getScreenY() + yOffset.get());
    }
}
