package de.fearnixx.lolbanpick.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.fearnixx.lolbanpick.Constants;
import de.fearnixx.lolbanpick.ShutdownListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class ConfigFX implements ShutdownListener, Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFX.class);
    public static final String CONFIG_FILE_NAME = "config.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @FXML
    private CheckBox cbScoreEnabled;

    @FXML
    private CheckBox cbSpellsEnabled;

    @FXML
    private CheckBox cbCoachesEnabled;

    @FXML
    private TextField blueScore;

    @FXML
    private TextField blueName;

    @FXML
    private TextField blueCoach;

    @FXML
    private TextField blueRGB1;

    @FXML
    private TextField blueRGB2;

    @FXML
    private TextField blueRGB3;

    @FXML
    private TextField redScore;

    @FXML
    private TextField redName;

    @FXML
    private TextField redCoach;

    @FXML
    private TextField redRGB1;

    @FXML
    private TextField redRGB2;

    @FXML
    private TextField redRGB3;

    @FXML
    private TextField logoPath;

    @FXML
    private Button logoChooseBtn;
    private FileChooser logoChooser = new FileChooser();

    @FXML
    private Button saveAndCloseBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logoPath.setDisable(true);
        logoChooser.setInitialDirectory(new File(".").getAbsoluteFile());
        final var extFilter = new FileChooser.ExtensionFilter("Image files", "*.png");
        logoChooser.getExtensionFilters().add(extFilter);
        logoChooser.setSelectedExtensionFilter(extFilter);
        logoChooser.setTitle("Choose logo.");

        logger.info("Attempting to read current configuration.");
        try (var reader = new FileReader(new File(Constants.PICKBAN_DIR, CONFIG_FILE_NAME))) {
            final var root = GSON.fromJson(reader, JsonObject.class);
            final var frontendSettings = root.getAsJsonObject(ConfigKeys.FRONTEND);

            cbScoreEnabled.setSelected(frontendSettings.get(ConfigKeys.ENABLE_SCORES).getAsBoolean());
            cbSpellsEnabled.setSelected(frontendSettings.get(ConfigKeys.ENABLE_SPELLS).getAsBoolean());
            cbCoachesEnabled.setSelected(frontendSettings.get(ConfigKeys.ENABLE_COACHES).getAsBoolean());

            final var teamBlue = frontendSettings.get(ConfigKeys.TEAM_BLUE).getAsJsonObject();
            blueScore.setText(Integer.toString(teamBlue.get(ConfigKeys.TEAM_SCORE).getAsInt()));
            blueName.setText(teamBlue.get(ConfigKeys.TEAM_NAME).getAsString());
            blueCoach.setText(teamBlue.get(ConfigKeys.TEAM_COACH).getAsString());

            final var teamRed = frontendSettings.get(ConfigKeys.TEAM_RED).getAsJsonObject();
            redScore.setText(Integer.toString(teamRed.get(ConfigKeys.TEAM_SCORE).getAsInt()));
            redName.setText(teamRed.get(ConfigKeys.TEAM_NAME).getAsString());
            redCoach.setText(teamRed.get(ConfigKeys.TEAM_COACH).getAsString());

        } catch (IOException e) {
            logger.error("Error loading configuration!", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onShutdown() {
    }

    public void saveAndClose() {
        saveAndCloseBtn.setDisable(true);
        if (save()) {
            ((Stage) saveAndCloseBtn.getScene().getWindow()).close();
        }
    }

    private boolean save() {
        JsonObject settings;
        try (var reader = new FileReader(new File(Constants.PICKBAN_DIR, CONFIG_FILE_NAME))) {
            settings = GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            logger.error("Failed to read config for saving!");
            return false;
        }

        try (var writer = new FileWriter(new File(Constants.PICKBAN_DIR, CONFIG_FILE_NAME))) {
            final var frontendConfig = settings.get(ConfigKeys.FRONTEND).getAsJsonObject();
            frontendConfig.addProperty(ConfigKeys.ENABLE_SCORES, cbScoreEnabled.isSelected());
            frontendConfig.addProperty(ConfigKeys.ENABLE_SPELLS, cbSpellsEnabled.isSelected());
            frontendConfig.addProperty(ConfigKeys.ENABLE_COACHES, cbCoachesEnabled.isSelected());

            final var teamBlue = frontendConfig.getAsJsonObject(ConfigKeys.TEAM_BLUE);
            teamBlue.addProperty(ConfigKeys.TEAM_SCORE, Integer.parseInt(blueScore.getText()));
            teamBlue.addProperty(ConfigKeys.TEAM_NAME, blueName.getText());
            teamBlue.addProperty(ConfigKeys.TEAM_COACH, blueCoach.getText());

            final var teamRed = frontendConfig.getAsJsonObject(ConfigKeys.TEAM_RED);
            teamRed.addProperty(ConfigKeys.TEAM_SCORE, Integer.parseInt(redScore.getText()));
            teamRed.addProperty(ConfigKeys.TEAM_NAME, redName.getText());
            teamRed.addProperty(ConfigKeys.TEAM_COACH, redCoach.getText());

            GSON.toJson(settings, writer);
        } catch (IOException e) {
            logger.error("Failed to save configuration!", e);
            return false;
        }

        if (!logoChooseBtn.isDisable()) {
            final var logoFile = new File(logoPath.getText());
            if (Files.exists(logoFile.toPath())) {
                logger.info("Attempting to copy logo file: {}", logoFile.toPath());
                try (var out = new FileOutputStream(new File(Constants.PICKBAN_EULAYOUT_DIR, "src/assets/Logo_Itemania_2019.png"))) {
                    Files.copy(logoFile.toPath(), out);
                } catch (AccessDeniedException e) {
                    logger.warn("Access to logo file denied. Is the overlay running?");
                } catch (IOException e) {
                    logger.error("Failed to copy logo file!", e);
                    return false;
                }
            }
        } else {
            logger.debug("Not setting logo as it was not allowed.");
        }
        return true;
    }

    public void openLogoChooser() {
        final var file = logoChooser.showOpenDialog(saveAndCloseBtn.getScene().getWindow());
        if (file != null) {
            logoPath.setText(file.toPath().toString());
        }
    }

    public void setAllowLogoChange(boolean allow) {
        logoChooseBtn.setDisable(!allow);
    }
}
