package timelog.preferences;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextInputControl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PreferenceMap {
    private Map<TextInputControl, String> textInputControls = new HashMap<>();
    private Map<ChoiceBox<?>, String> choiceBoxes = new HashMap<>();
    private Map<CheckBox, String> checkBoxes = new HashMap<>();

    public void mapTo(CheckBox control, String key) {
        checkBoxes.put(control, key);
        control.setSelected(Preferences.get(key).equals("true"));
    }

    public void mapTo(TextInputControl control, String key) {
        textInputControls.put(control, key);
        control.setText(Preferences.get(key));
    }

    public <T> void mapTo(ChoiceBox<T> control, String key, Function<String, T> fromString) {
        choiceBoxes.put(control, key);
        control.setValue(fromString.apply(Preferences.get(key)));
    }

    public void dumpPreferences() {
        choiceBoxes.forEach((control, key) -> Preferences.set(key, control.getValue().toString()));
        textInputControls.forEach((control, key) -> Preferences.set(key, control.getText()));
        checkBoxes.forEach((control, key) -> Preferences.set(key, control.isSelected() ? "true" : "false"));
    }
}