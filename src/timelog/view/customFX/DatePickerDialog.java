package timelog.view.customFX;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;
import java.util.function.Function;

public class DatePickerDialog extends Dialog<LocalDate> {
    @SuppressWarnings("FieldCanBeLocal")
    // this is no local variable so that it will not be removed from the heap by the garbage collector, when it is still needed
    private final ObservableValue<Boolean> okEnabled;

    public DatePickerDialog(Function<ObservableValue<LocalDate>, ObservableValue<Boolean>> condition) {
        this(condition, null);
    }

    public DatePickerDialog(Function<ObservableValue<LocalDate>, ObservableValue<Boolean>> condition, String label) {
        super();
        setTitle("DatePicker");
        setHeaderText("Pick a Date" + (label != null ? ": " + label : ""));

        final DatePicker datePicker = new DatePicker();
        Util.applyAfterFocusLost(datePicker);
        getDialogPane().setContent(new BorderPane(datePicker));

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okEnabled = condition.apply(datePicker.valueProperty());
        okEnabled.addListener((observable, oldValue, newValue) -> okButton.setDisable(!newValue));
        okButton.setDisable(!okEnabled.getValue());

        setResultConverter(buttonType -> ButtonType.OK.equals(buttonType) ? datePicker.getValue() : null);
    }
}
