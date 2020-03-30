package timelog.view;

import javafx.application.Application;
import javafx.stage.Stage;
import timelog.model.db.Database;
import timelog.preferences.Preferences;
import timelog.view.customFX.ErrorAlert;

import java.io.IOException;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("TimeLog");
        stage.setScene(new LoginScene(() -> stage.setScene(new MainScene()), true));
        stage.show();
    }

    @Override
    public void init() throws IOException {
        Preferences.loadPropertiesFile(Preferences.DEFAULTS_FILE_NAME);
        Preferences.loadPropertiesFile(Preferences.FILE_NAME);
        Database.setErrorHandler(ErrorAlert::show);
    }

    @Override
    public void stop() throws IOException {
        Preferences.savePropertiesFile(Preferences.FILE_NAME);
    }
}
