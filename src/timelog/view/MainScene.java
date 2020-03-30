package timelog.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import timelog.model.LogEntry;
import timelog.statistic.Statistics;
import timelog.view.edit.LogEntryDialog;
import timelog.view.statistic.Report;

import java.time.LocalDate;
import java.util.function.Supplier;

public class MainScene extends Scene {

    public MainScene() {
        super(new BorderPane(), 350, Region.USE_COMPUTED_SIZE);

        final LogEntryList logEntryList = new LogEntryList();
        logEntryList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOn(LocalDate.now()));

        final CurrentEntryDisplay currentEntryDisplay = new CurrentEntryDisplay(logEntry -> {
            if (logEntry.getEnd().toLocalDate().equals(LocalDate.now())) logEntryList.getEntries().add(logEntry);
        });
        HBox.setHgrow(currentEntryDisplay, Priority.ALWAYS);

        final BorderPane borderPane = (BorderPane) getRoot();
        borderPane.setTop(getMenuBar());
        borderPane.setCenter(logEntryList);
        BorderPane.setMargin(logEntryList, new Insets(10));
        borderPane.setBottom(currentEntryDisplay);
        BorderPane.setMargin(currentEntryDisplay, new Insets(0, 10, 10, 10));
    }

    private MenuBar getMenuBar() {

        final MenuItem editAll = new MenuItem("Edit All");
        editAll.setOnAction(event -> LogEntry.FACTORY.getAll().forEach(logEntry -> new LogEntryDialog(logEntry).showAndWait()));

        return new MenuBar(
                new Menu("Statistic", null,
                        getReportMenuItem("Today",
                                () -> new Report(Statistics.activitiesToday(), Statistics.qualityTimeToday())),
                        getReportMenuItem("Last 7 days",
                                () -> new Report(Statistics.activitiesThisWeek(), Statistics.qualityTimeThisWeek()))),
                new Menu("Tools", null, editAll));
    }

    private MenuItem getReportMenuItem(final String name, final Supplier<Report> report) {
        final MenuItem thisWeekMenuItem = new MenuItem(name);
        thisWeekMenuItem.setOnAction(event -> report.get().show());
        return thisWeekMenuItem;
    }
}
