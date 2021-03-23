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
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ConfigFX implements ShutdownListener, Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFX.class);
    public static final String CONFIG_FILE_NAME = "config.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private static final Pattern colorPattern = Pattern.compile("^rgb\\( *(\\d{1,3}) *, *(\\d{1,3}) *, *(\\d{1,3}) *\\)$");

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
    private ColorPicker blueColorPicker;

    @FXML
    private TextField redScore;

    @FXML
    private TextField redName;

    @FXML
    private TextField redCoach;

    @FXML
    private ColorPicker redColorPicker;

    @FXML
    private CheckBox cbSwapColors;

    @FXML
    private TextField logoPath;

    @FXML
    private Button logoChooseBtn;

    private final FileChooser logoChooser = new FileChooser();

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
        cbSwapColors.setSelected(true);

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
            blueColorPicker.setValue(parseColor(teamBlue.get(ConfigKeys.TEAM_COLOR).getAsString()));

            final var teamRed = frontendSettings.get(ConfigKeys.TEAM_RED).getAsJsonObject();
            redScore.setText(Integer.toString(teamRed.get(ConfigKeys.TEAM_SCORE).getAsInt()));
            redName.setText(teamRed.get(ConfigKeys.TEAM_NAME).getAsString());
            redCoach.setText(teamRed.get(ConfigKeys.TEAM_COACH).getAsString());
            redColorPicker.setValue(parseColor(teamRed.get(ConfigKeys.TEAM_COLOR).getAsString()));
        } catch (IOException e) {
            logger.error("Error loading configuration!", e);
            throw new RuntimeException(e);
        }
    }

    private Color parseColor(String colorStr) {
        final var matcher = colorPattern.matcher(colorStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid RGB-Color: " + colorStr);
        }

        try {
            var redValue = Integer.parseInt(matcher.group(1));
            var greenValue = Integer.parseInt(matcher.group(2));
            var blueValue = Integer.parseInt(matcher.group(3));
            return Color.rgb(redValue, greenValue, blueValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Failed to parse double from rgb part of: " + colorStr);
        }
    }

    private String serializeColor(Color color) {
        Function<Double, Integer> rgbIze = d -> Math.toIntExact(Math.round(d * 255));
        return String.format("rgb(%s,%s,%s)",
                rgbIze.apply(color.getRed()),
                rgbIze.apply(color.getGreen()),
                rgbIze.apply(color.getBlue())
        );
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
            logger.info("Attempting to write current configuration.");
            final var frontendConfig = settings.get(ConfigKeys.FRONTEND).getAsJsonObject();
            frontendConfig.addProperty(ConfigKeys.ENABLE_SCORES, cbScoreEnabled.isSelected());
            frontendConfig.addProperty(ConfigKeys.ENABLE_SPELLS, cbSpellsEnabled.isSelected());
            frontendConfig.addProperty(ConfigKeys.ENABLE_COACHES, cbCoachesEnabled.isSelected());

            final var teamBlue = frontendConfig.getAsJsonObject(ConfigKeys.TEAM_BLUE);
            teamBlue.addProperty(ConfigKeys.TEAM_SCORE, Integer.parseInt(blueScore.getText()));
            teamBlue.addProperty(ConfigKeys.TEAM_NAME, blueName.getText());
            teamBlue.addProperty(ConfigKeys.TEAM_COACH, blueCoach.getText());
            teamBlue.addProperty(ConfigKeys.TEAM_COLOR, serializeColor(blueColorPicker.getValue()));

            final var teamRed = frontendConfig.getAsJsonObject(ConfigKeys.TEAM_RED);
            teamRed.addProperty(ConfigKeys.TEAM_SCORE, Integer.parseInt(redScore.getText()));
            teamRed.addProperty(ConfigKeys.TEAM_NAME, redName.getText());
            teamRed.addProperty(ConfigKeys.TEAM_COACH, redCoach.getText());
            teamRed.addProperty(ConfigKeys.TEAM_COLOR, serializeColor(redColorPicker.getValue()));

            GSON.toJson(settings, writer);
        } catch (IOException e) {
            logger.error("Failed to save configuration!", e);
            return false;
        }

        if (!logoChooseBtn.isDisable() && !logoPath.getText().isBlank()) {
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

    public void sideSwap() {
        final var tempBlueName = blueName.getText();
        final var tempBlueScore = blueScore.getText();
        final var tempBlueCoach = blueCoach.getText();
        final var tempBlueColor = blueColorPicker.getValue();

        blueName.setText(redName.getText());
        blueScore.setText(redScore.getText());
        blueCoach.setText(redCoach.getText());

        redName.setText(tempBlueName);
        redScore.setText(tempBlueScore);
        redCoach.setText(tempBlueCoach);

        if (cbSwapColors.isSelected()) {
            blueColorPicker.setValue(redColorPicker.getValue());
            redColorPicker.setValue(tempBlueColor);
        }
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
