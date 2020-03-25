package timelog.view.customFX;

import javafx.beans.Observable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import timelog.model.Activity;
import timelog.model.LogEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

class LogEntryDialog extends ObjectDialog<LogEntry> {
    private final CreatingChoiceBox<Activity> activity;
    private final TextField what;
    private final DatePicker startDate;
    private final TimeTextField startTime;
    private final DatePicker endDate;
    private final TimeTextField endTime;

    public LogEntryDialog() {
        this(null);
    }

    public LogEntryDialog(LogEntry editedObject) {
        super("Log Entry", editedObject, true);

        activity = gridPane2C.addRow("Type", new CreatingChoiceBox<>(Activity.FACTORY.getAll(), ActivityDialog::new, ActivityDialog::new));
        activity.setValue(Activity.FACTORY.getForId(0));
        activity.valueProperty().addListener(this::invalidated);

        what = gridPane2C.addRow("What", new TextField());
        what.textProperty().addListener(this::invalidated);
        what.requestFocus();

        startDate = gridPane2C.addRow("Start", new DatePicker(LocalDate.now()));
        startDate.valueProperty().addListener(this::invalidated);

        startTime = gridPane2C.addRow("", new TimeTextField(LocalTime.now()));
        startTime.valueProperty().addListener(this::invalidated);

        endDate = gridPane2C.addRow("End", new DatePicker(LocalDate.now()));
        endTime = gridPane2C.addRow("", new TimeTextField(null));

        if (editedObject != null) {
            activity.setValue(editedObject.getActivity());
            what.setText(editedObject.getWhat());
            startDate.setValue(editedObject.getStart().toLocalDate());
            startTime.setValue(editedObject.getStart().toLocalTime());
            if (editedObject.getEnd() != null) {
                endDate.setValue(editedObject.getEnd().toLocalDate());
                endTime.setValue(editedObject.getEnd().toLocalTime());
            }
        }
    }

    private void invalidated(Observable observable) {
        okButton.setDisable(activity.getValue() == null
                || what.getText().isBlank()
                || startTime.getValue() == null
                || startDate.getValue() == null
                || endDate.getValue() == null
        );
    }

    @Override
    protected LogEntry createNew() {
        return LogEntry.FACTORY.createNew(
                activity.getValue(),
                what.getText(),
                LocalDateTime.of(startDate.getValue(), startTime.getValue()),
                endTime.getValue() == null ? null : LocalDateTime.of(endDate.getValue(), endTime.getValue())
        );
    }

    @Override
    protected boolean save() {
        editedObject.setActivity(activity.getValue());
        editedObject.setWhat(what.getText());
        editedObject.setStart(LocalDateTime.of(startDate.getValue(), startTime.getValue()));
        editedObject.setEnd(endTime.getValue() == null ? null : LocalDateTime.of(endDate.getValue(), endTime.getValue()));
        return LogEntry.FACTORY.update(editedObject);
    }
}
