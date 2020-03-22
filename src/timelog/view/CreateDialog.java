package timelog.view;

import javafx.beans.Observable;
import javafx.scene.control.*;
import timelog.model.LogEntry;
import timelog.view.customFX.GridPane2C;
import timelog.view.customFX.TimeTextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

class CreateDialog extends Dialog<LogEntry> {
    private final TextField what;
    private final DatePicker date;
    private final TimeTextField time;
    private final Button okButton;

    public CreateDialog() {
        super();
        setTitle("New Activity");
        setHeaderText("Start a New Activity");

        GridPane2C gridPane2C = new GridPane2C(10);
        what = gridPane2C.addRow("What", new TextField());
        date = gridPane2C.addRow("Day", new DatePicker(LocalDate.now()));
        time = gridPane2C.addRow("Start", new TimeTextField(LocalTime.now()));
        getDialogPane().setContent(gridPane2C);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        what.textProperty().addListener(this::invalidated);
        date.valueProperty().addListener(this::invalidated);
        time.valueProperty().addListener(this::invalidated);
        invalidated(null);

        what.requestFocus();

        setResultConverter(this::getValue);
    }

    private void invalidated(Observable observable) {
        okButton.setDisable(what.getText().isBlank() || time.getValue() == null || date.getValue() == null);
    }

    private LogEntry getValue(ButtonType buttonType) {
        return buttonType.equals(ButtonType.OK)
                ? LogEntry.FACTORY.createNew(
                what.getText(),
                LocalDateTime.of(date.getValue(), time.getValue()),
                null)
                : null;
    }
}
