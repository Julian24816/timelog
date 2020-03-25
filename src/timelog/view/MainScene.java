package timelog.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import timelog.model.LogEntry;

import java.time.LocalDate;

public class MainScene extends Scene {

    public MainScene() {
        super(new BorderPane(), 350, Region.USE_COMPUTED_SIZE);

        final LogEntryList logEntryList = new LogEntryList();
        logEntryList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOn(LocalDate.now()));

        final CurrentEntryDisplay currentEntryDisplay = new CurrentEntryDisplay(null);
        currentEntryDisplay.setActivity(LogEntry.FACTORY.getUnfinishedEntry());
        HBox.setHgrow(currentEntryDisplay, Priority.ALWAYS);

        final CurrentEntryButton button = new CurrentEntryButton(logEntryList, currentEntryDisplay);

        final BorderPane borderPane = (BorderPane) getRoot();
        borderPane.setPadding(new Insets(10));
        borderPane.setCenter(logEntryList);
        BorderPane.setMargin(logEntryList, new Insets(0, 0, 10, 0));
        borderPane.setBottom(new HBox(10, currentEntryDisplay, button));
    }
}
