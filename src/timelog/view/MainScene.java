package timelog.view;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import timelog.model.LogEntry;
import timelog.view.customFX.CurrentActivity;

import java.time.LocalDateTime;
import java.util.Optional;

public class MainScene extends Scene {

    private final CurrentActivity currentActivity = new CurrentActivity(null);
    private final Button button = new Button("Start");

    public MainScene() {
        super(new VBox(10));
        VBox vBox = (VBox) getRoot();
        vBox.setPadding(new Insets(10));
        vBox.getChildren().addAll(currentActivity, button);

        currentActivity.activityProperty().addListener(this::currentActivityChanged);
        button.setOnAction(this::onButtonPress);

        currentActivity.setActivity(LogEntry.FACTORY.getUnfinishedEntry());
    }

    private void currentActivityChanged(Observable observable) {
        button.setText(currentActivity.getActivity() == null ? "Start" : "Stop");
    }

    private void onButtonPress(ActionEvent event) {
        if (currentActivity.getActivity() == null) {
            final Optional<LogEntry> logEntry = new CreateDialog().showAndWait();
            logEntry.ifPresent(currentActivity::setActivity);
        } else {
            final Optional<LocalDateTime> endTime = new EndTimeDialog().showAndWait();
            endTime.ifPresent(end -> {
                currentActivity.getActivity().endProperty().setValue(end);
                if (LogEntry.FACTORY.save(currentActivity.getActivity())) currentActivity.setActivity(null);
            });
        }
    }
}
