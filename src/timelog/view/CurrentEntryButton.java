package timelog.view;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import timelog.model.LogEntry;
import timelog.view.edit.LogEntryDialog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class CurrentEntryButton extends Button {
    private final LogEntryList logEntryList;
    private final CurrentEntryDisplay currentEntryDisplay;

    public CurrentEntryButton(LogEntryList logEntryList, CurrentEntryDisplay currentEntryDisplay) {
        super("New");
        this.logEntryList = logEntryList;
        this.currentEntryDisplay = currentEntryDisplay;
        setOnAction(this::onButtonPress);

        currentEntryDisplay.entryProperty().addListener(this::currentActivityChanged);
        currentActivityChanged(null);

        setMaxHeight(Double.MAX_VALUE);
        heightProperty().addListener((observable, oldValue, newValue) -> setPrefWidth(newValue.doubleValue()));
    }

    private void onButtonPress(ActionEvent event) {
        if (currentEntryDisplay.getEntry() == null) {
            final Optional<LogEntry> logEntry = new LogEntryDialog().showAndWait();
            logEntry.ifPresent(value -> {
                if (value.getEnd() == null) currentEntryDisplay.setEntry(value);
                else if (value.getEnd().toLocalDate().equals(LocalDate.now())) logEntryList.getEntries().add(value);
            });
        } else {
            final Optional<LocalDateTime> endTime = new EndTimeDialog().showAndWait();
            endTime.ifPresent(end -> {
                currentEntryDisplay.getEntry().endProperty().setValue(end);
                if (LogEntry.FACTORY.update(currentEntryDisplay.getEntry())) {
                    if (currentEntryDisplay.getEntry().getEnd().toLocalDate().equals(LocalDate.now()))
                        logEntryList.getEntries().add(currentEntryDisplay.getEntry());
                    currentEntryDisplay.setEntry(LogEntry.FACTORY.getUnfinishedEntry());
                }
            });
        }
    }

    private void currentActivityChanged(Observable observable) {
        setText(currentEntryDisplay.getEntry() == null ? "New" : "Stop");
    }
}
