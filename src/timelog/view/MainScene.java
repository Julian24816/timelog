package timelog.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import timelog.model.LogEntry;
import timelog.view.customFX.ActivityList;
import timelog.view.customFX.CurrentActivity;
import timelog.view.customFX.CurrentActivityButton;

import java.time.LocalDate;

public class MainScene extends Scene {

    public MainScene() {
        super(new BorderPane(), 350, Region.USE_COMPUTED_SIZE);

        final ActivityList activityList = new ActivityList();
        activityList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOn(LocalDate.now()));

        final CurrentActivity currentActivity = new CurrentActivity(null);
        currentActivity.setActivity(LogEntry.FACTORY.getUnfinishedEntry());
        HBox.setHgrow(currentActivity, Priority.ALWAYS);

        final CurrentActivityButton button = new CurrentActivityButton(activityList, currentActivity);

        final BorderPane borderPane = (BorderPane) getRoot();
        borderPane.setPadding(new Insets(10));
        borderPane.setCenter(activityList);
        BorderPane.setMargin(activityList, new Insets(0, 0, 10, 0));
        borderPane.setBottom(new HBox(10, currentActivity, button));
    }
}
