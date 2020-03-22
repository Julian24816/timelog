package timelog.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import timelog.view.customFX.ErrorAlert;
import timelog.view.customFX.GridPane2C;
import timelog.model.Database;
import timelog.model.PreferenceMap;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

public class LoginScene extends Scene {
    public LoginScene(Runnable onLogin) {
        super(new GridPane2C(10));
        GridPane2C gridPane2C = (GridPane2C) getRoot();
        gridPane2C.setPadding(new Insets(10));
        PreferenceMap preferenceMap = new PreferenceMap();

        ChoiceBox<DriverChoice> driver = gridPane2C.addRow("Driver", new ChoiceBox<>());
        driver.getItems().addAll(DriverChoice.values());
        preferenceMap.mapTo(driver, "DatabaseDriver", DriverChoice::valueOf);

        TextField database = gridPane2C.addRow("Database", new TextField());
        database.setPrefColumnCount(20);
        preferenceMap.mapTo(database, "DatabaseURL");

        TextField username = gridPane2C.addRow("Username", new TextField());
        preferenceMap.mapTo(username, "DatabaseUsername");

        PasswordField password = gridPane2C.addRow("Password", new PasswordField());
        preferenceMap.mapTo(database, "DatabasePassword");

        Button login = gridPane2C.addRow("", new Button("Login"));
        login.setDefaultButton(true);
        login.setOnAction(actionEvent -> {
            Database.init(driver.getValue().name + ":" + database.getText(), username.getText(), password.getText());
            try {
                Database.execFile(Paths.get("db", "timelog.sql"));
                preferenceMap.dumpPreferences();
                onLogin.run();
            } catch (SQLException | IOException e) {
                ErrorAlert.show(e);
            }
        });
    }

    private enum DriverChoice {
        SQLite("jdbc:sqlite");

        private final String name;

        DriverChoice(String name) {
            this.name = name;
        }
    }
}
