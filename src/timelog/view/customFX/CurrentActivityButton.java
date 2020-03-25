package timelog.view.customFX;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import timelog.model.LogEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class CurrentActivityButton extends Button {
    private final LogEntryList logEntryList;
    private final CurrentActivity currentActivity;

    public CurrentActivityButton(LogEntryList logEntryList, CurrentActivity currentActivity) {
        super("Start");
        this.logEntryList = logEntryList;
        this.currentActivity = currentActivity;
        setOnAction(this::onButtonPress);

        currentActivity.activityProperty().addListener(this::currentActivityChanged);
        currentActivityChanged(null);

        setMaxHeight(Double.MAX_VALUE);
        heightProperty().addListener((observable, oldValue, newValue) -> setPrefWidth(newValue.doubleValue()));
    }

    private void currentActivityChanged(Observable observable) {
        setText(currentActivity.getActivity() == null ? "Start" : "Stop");
    }

    private void onButtonPress(ActionEvent event) {
        if (currentActivity.getActivity() == null) {
            final Optional<LogEntry> logEntry = new LogEntryDialog().showAndWait();
            logEntry.ifPresent(value -> {
                if (value.getEnd() == null) currentActivity.setActivity(value);
                else if (value.getEnd().toLocalDate().equals(LocalDate.now())) logEntryList.getEntries().add(value);
            });
        } else {
            final Optional<LocalDateTime> endTime = new EndTimeDialog().showAndWait();
            endTime.ifPresent(end -> {
                currentActivity.getActivity().endProperty().setValue(end);
                if (LogEntry.FACTORY.update(currentActivity.getActivity())) {
                    if (currentActivity.getActivity().getEnd().toLocalDate().equals(LocalDate.now()))
                        logEntryList.getEntries().add(currentActivity.getActivity());
                    currentActivity.setActivity(LogEntry.FACTORY.getUnfinishedEntry());
                }
            });
        }
    }
}
