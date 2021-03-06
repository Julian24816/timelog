package timelog.view;

import javafx.application.Application;
import javafx.stage.Stage;
import timelog.model.db.Database;
import timelog.preferences.Preferences;
import timelog.view.customFX.ErrorAlert;

import java.io.IOException;
import java.time.LocalTime;

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
        setDefaultPreferences();
        Preferences.loadPropertiesFile(Preferences.FILE_NAME);
        Database.setErrorHandler(ErrorAlert::show);
    }

    private void setDefaultPreferences() {
        Preferences.set("DatabaseDriver", "SQLite");
        Preferences.set("DatabaseURL", ":memory:");
        Preferences.set("DatabaseUsername", "");
        Preferences.set("DatabasePassword", "");
        Preferences.set("AutomaticLogin", false);
        Preferences.set("SleepID", -1);
        Preferences.set("SleepLineHeight", 20);
        Preferences.set("MinuteToPixelScale", 2f);
        Preferences.set("MinuteMarkEvery", 30);
        Preferences.set("MinuteMarkWidth", 10);
        Preferences.set("MinuteMarkColor", "BLACK");
        Preferences.set("StartOfDay", LocalTime.of(4, 0));
    }

    @Override
    public void stop() throws IOException {
        Preferences.savePropertiesFile(Preferences.FILE_NAME);
    }
}
