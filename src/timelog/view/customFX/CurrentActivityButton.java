package timelog.view.customFX;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import timelog.model.LogEntry;

import java.time.LocalDateTime;
import java.util.Optional;

public class CurrentActivityButton extends Button {
    private final ActivityList activityList;
    private final CurrentActivity currentActivity;

    public CurrentActivityButton(ActivityList activityList, CurrentActivity currentActivity) {
        super("Start");
        this.activityList = activityList;
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
            final Optional<LogEntry> logEntry = new CreateDialog().showAndWait();
            logEntry.ifPresent(currentActivity::setActivity);
        } else {
            final Optional<LocalDateTime> endTime = new EndTimeDialog().showAndWait();
            endTime.ifPresent(end -> {
                currentActivity.getActivity().endProperty().setValue(end);
                if (LogEntry.FACTORY.update(currentActivity.getActivity())) {
                    activityList.getEntries().add(currentActivity.getActivity());
                    currentActivity.setActivity(null);
                }
            });
        }
    }
}
