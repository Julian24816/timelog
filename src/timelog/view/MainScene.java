package timelog.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import timelog.model.LogEntry;
import timelog.view.customFX.CustomBindings;
import timelog.view.customFX.DatePickerDialog;
import timelog.view.edit.LogEntryDialog;
import timelog.view.edit.PreferencesDialog;
import timelog.view.insight.LookAtDayDialog;
import timelog.view.insight.Report;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

public class MainScene extends Scene {

    private final LogEntryList logEntryList;

    public MainScene() {
        super(new BorderPane(), 350, Region.USE_COMPUTED_SIZE);

        logEntryList = new LogEntryList();
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
        final MenuItem lookAt = new MenuItem("Look At Day ...");
        lookAt.setOnAction(event -> new DatePickerDialog(localDateObservableValue -> CustomBindings.isBefore(localDateObservableValue, LocalDate.now()))
                .showAndWait().ifPresent(date -> new LookAtDayDialog(date).show()));

        final MenuItem editAll = new MenuItem("Edit All Entries");
        editAll.setOnAction(event -> LogEntry.FACTORY.getAll().forEach(logEntry -> new LogEntryDialog(logEntry).showAndWait()));

        final MenuItem reload = new MenuItem("Reload List");
        reload.setOnAction(event -> {
            logEntryList.getEntries().clear();
            logEntryList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOn(LocalDate.now()));
        });

        final MenuItem refreshCanvas = new MenuItem("Redraw Minute Marks");
        refreshCanvas.setOnAction(event -> logEntryList.refreshCanvas());

        final MenuItem preferences = new MenuItem("Preferences");
        preferences.setOnAction(event -> new PreferencesDialog().showAndWait()
                .filter(buttonType -> buttonType.equals(ButtonType.OK))
                .ifPresent(ok -> List.of(reload, refreshCanvas).forEach(MenuItem::fire)));

        return new MenuBar(
                new Menu("Report", null,
                        reportMenuItem("Today", Report::today),
                        reportMenuItem("Yesterday", Report::yesterday),
                        reportMenuItem("Last 7 days", Report::last7days),
                        reportMenuItem("Current Week", Report::currentWeek),
                        reportMenuItem("Previous Week", Report::previousWeek)
                ),
                new Menu("Tools", null, lookAt, editAll, reload, refreshCanvas, preferences)
        );
    }

    private MenuItem reportMenuItem(final String yesterday, final Supplier<Report> report) {
        final MenuItem menuItem = new MenuItem(yesterday);
        menuItem.setOnAction(event -> report.get().show());
        return menuItem;
    }

}
