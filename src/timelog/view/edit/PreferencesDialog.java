package timelog.view.edit;

import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import timelog.preferences.PreferenceMap;
import timelog.view.customFX.CustomBindings;
import timelog.view.customFX.GridPane2C;

public class PreferencesDialog extends Dialog<ButtonType> {

    private final BooleanExpression okEnabled;

    public PreferencesDialog() {
        super();
        setTitle("Preferences");
        setHeaderText("Edit Preferences");

        PreferenceMap preferenceMap = new PreferenceMap();
        GridPane2C gridPane2C = new GridPane2C(10);
        getDialogPane().setContent(gridPane2C);

        final TextField scaling = gridPane2C.addRow("Minute To Pixel Scale", new TextField());
        preferenceMap.mapTo(scaling, "MinuteToPixelScale");

        final TextField sleepID = gridPane2C.addRow("ID of Sleep Activity", new TextField());
        preferenceMap.mapTo(sleepID, "SleepID");
        sleepID.setPromptText("set to -1 to disable");

        final TextField sleepLineHeight = gridPane2C.addRow("Sleep Line Height", new TextField());
        preferenceMap.mapTo(sleepLineHeight, "SleepLineHeight");

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setOnAction(event -> preferenceMap.dumpPreferences());

        okEnabled = CustomBindings.matches(scaling, "\\d+(\\.\\d+)?")
                .and(CustomBindings.matches(sleepID, "-1|\\d+"))
                .and(CustomBindings.matches(sleepLineHeight, "\\d+"));
        okEnabled.addListener(observable -> okButton.setDisable(!okEnabled.getValue()));
    }

}
