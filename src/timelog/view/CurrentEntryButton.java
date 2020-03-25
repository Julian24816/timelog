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

        currentEntryDisplay.activityProperty().addListener(this::currentActivityChanged);
        currentActivityChanged(null);

        setMaxHeight(Double.MAX_VALUE);
        heightProperty().addListener((observable, oldValue, newValue) -> setPrefWidth(newValue.doubleValue()));
    }

    private void currentActivityChanged(Observable observable) {
        setText(currentEntryDisplay.getActivity() == null ? "New" : "Stop");
    }

    private void onButtonPress(ActionEvent event) {
        if (currentEntryDisplay.getActivity() == null) {
            final Optional<LogEntry> logEntry = new LogEntryDialog().showAndWait();
            logEntry.ifPresent(value -> {
                if (value.getEnd() == null) currentEntryDisplay.setActivity(value);
                else if (value.getEnd().toLocalDate().equals(LocalDate.now())) logEntryList.getEntries().add(value);
            });
        } else {
            final Optional<LocalDateTime> endTime = new EndTimeDialog().showAndWait();
            endTime.ifPresent(end -> {
                currentEntryDisplay.getActivity().endProperty().setValue(end);
                if (LogEntry.FACTORY.update(currentEntryDisplay.getActivity())) {
                    if (currentEntryDisplay.getActivity().getEnd().toLocalDate().equals(LocalDate.now()))
                        logEntryList.getEntries().add(currentEntryDisplay.getActivity());
                    currentEntryDisplay.setActivity(LogEntry.FACTORY.getUnfinishedEntry());
                }
            });
        }
    }
}
