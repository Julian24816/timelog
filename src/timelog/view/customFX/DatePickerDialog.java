package timelog.view.customFX;

import javafx.beans.value.ObservableBooleanValue;
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
    private final ObservableBooleanValue okEnabled;

    public DatePickerDialog(Function<ObservableValue<LocalDate>, ObservableBooleanValue> condition) {
        super();
        setTitle("DatePicker");
        setHeaderText("Pick a Date");

        final DatePicker datePicker = new DatePicker();
        getDialogPane().setContent(new BorderPane(datePicker));

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okEnabled = condition.apply(datePicker.valueProperty());
        okEnabled.addListener((observable, oldValue, newValue) -> okButton.setDisable(!newValue));
        okButton.setDisable(!okEnabled.get());

        setResultConverter(buttonType -> ButtonType.OK.equals(buttonType) ? datePicker.getValue() : null);
    }
}
