package timelog.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import timelog.model.LogEntry;
import timelog.view.customFX.ActivityList;
import timelog.view.customFX.CurrentActivity;
import timelog.view.customFX.CurrentActivityButton;

import java.time.LocalDate;

public class MainScene extends Scene {

    public MainScene() {
        super(new VBox(10));

        final ActivityList activityList = new ActivityList();
        activityList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOn(LocalDate.now()));

        final CurrentActivity currentActivity = new CurrentActivity(null);
        currentActivity.setActivity(LogEntry.FACTORY.getUnfinishedEntry());
        HBox.setHgrow(currentActivity, Priority.ALWAYS);

        final CurrentActivityButton button = new CurrentActivityButton(activityList, currentActivity);

        final VBox vBox = (VBox) getRoot();
        vBox.setPadding(new Insets(10));
        vBox.getChildren().addAll(activityList, new HBox(10, currentActivity, button));
    }
}
