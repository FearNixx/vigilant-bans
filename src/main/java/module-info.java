module lolbanpick.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.net.http;
    requires com.google.gson;

    opens de.fearnixx.lolbanpick.installer to javafx.fxml;
    opens de.fearnixx.lolbanpick.runner to javafx.fxml;
    opens de.fearnixx.lolbanpick.config to javafx.fxml;

    exports de.fearnixx.lolbanpick;
}