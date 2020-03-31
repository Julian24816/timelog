package timelog.view;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.VLineTo;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import timelog.model.Activity;
import timelog.model.LogEntry;
import timelog.model.MeansOfTransport;
import timelog.preferences.Preferences;
import timelog.view.customFX.CustomBindings;
import timelog.view.customFX.ErrorAlert;
import timelog.view.customFX.JoiningTextFlow;
import timelog.view.customFX.TimeText;
import timelog.view.edit.LogEntryDialog;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Consumer;

public class LogEntryList extends ScrollPane {

    private final VBox vBox = new VBox();
    private final Text placeholder = new Text("no activities");

    private final ObservableList<LogEntry> entries = FXCollections.observableArrayList();

    public LogEntryList() {
        super();
        VBox.setMargin(placeholder, new Insets(20));
        vBox.getChildren().add(placeholder);
        setContent(vBox);
        setFitToWidth(true);
        setPrefHeight(500);

        entries.addListener(this::onListChanged);
    }

    private void onListChanged(ListChangeListener.Change<? extends LogEntry> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be permutated"));
            } else if (c.wasUpdated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be updated"));
            } else {
                for (LogEntry added : c.getAddedSubList()) addEntry(added);
                for (LogEntry removed : c.getRemoved()) removeEntry(removed);
            }
        }
    }

    private void addEntry(LogEntry entry) {
        vBox.getChildren().remove(placeholder);
        vBox.getChildren().add(new ActivityLine(entry));
    }

    private void removeEntry(LogEntry removed) {
        vBox.getChildren().remove(new ActivityLine(removed));
        if (vBox.getChildren().isEmpty())
            vBox.getChildren().add(placeholder);
    }

    public ObservableList<LogEntry> getEntries() {
        return entries;
    }

    private static final class ActivityLine extends HBox {
        private static final double TIME_TEXT_WIDTH = 30, TIME_TEXT_HEIGHT = 16,
                DETAILS_VISIBLE_HEIGHT = 16;
        private final LogEntry entry;
        private BooleanBinding detailsVisibility;

        private ActivityLine(LogEntry entry) {
            super(10);
            this.entry = entry;
            createLayout(entry);
            setOnMouseClicked(this::onMouseClicked);
            backgroundProperty().bind(CustomBindings.apply(
                    CustomBindings.select(entry.activityProperty(), Activity::colorProperty),
                    color -> new Background(new BackgroundFill(Color.valueOf(color), null, null))));
        }

        private void createLayout(LogEntry entry) {
            final VBox time = getTimeVBox(entry, entry.getActivity().getId() == Preferences.getInt("SleepID") ? Preferences.getInt("SleepLineHeight") : -1);
            final TextFlow details = getDetails(entry);
            detailsVisibility = time.heightProperty().greaterThanOrEqualTo(DETAILS_VISIBLE_HEIGHT);
            detailsVisibility.addListener(observable -> {
                getChildren().remove(details);
                if (detailsVisibility.get()) getChildren().add(details);
            });
            getChildren().add(time);
        }

        private VBox getTimeVBox(LogEntry entry, double fixedLineHeight) {
            final TimeText start = new TimeText();
            start.valueProperty().bind(entry.startProperty());
            final TimeText end = new TimeText();
            end.valueProperty().bind(entry.endProperty());
            final VLineTo vLineTo = new VLineTo(fixedLineHeight);
            final Path line = new Path(new MoveTo(0, 0), vLineTo);
            final VBox time = new VBox(line);
            time.setAlignment(Pos.CENTER);
            time.setPrefWidth(TIME_TEXT_WIDTH);

            Consumer<Double> applyLineHeight = lineHeight -> {
                time.getChildren().removeAll(start, end);
                if (lineHeight > TIME_TEXT_HEIGHT * 2) {
                    time.getChildren().add(0, start);
                    time.getChildren().add(end);
                    vLineTo.setY(lineHeight - TIME_TEXT_HEIGHT * 2);
                } else if (lineHeight > TIME_TEXT_HEIGHT) {
                    time.getChildren().add(end);
                    vLineTo.setY(lineHeight - TIME_TEXT_HEIGHT);
                } else vLineTo.setY(lineHeight);
            };

            if (fixedLineHeight >= 0) applyLineHeight.accept(fixedLineHeight);
            else {
                InvalidationListener invalidated = observable -> {
                    if (entry.getEnd() == null) return;
                    final long minutes = this.entry.getStart().until(this.entry.getEnd(), ChronoUnit.MINUTES);
                    final double lineHeight = minutes / Preferences.getDouble("MinuteToPixelScale");
                    applyLineHeight.accept(lineHeight);
                };
                entry.startProperty().addListener(invalidated);
                entry.endProperty().addListener(invalidated);
                invalidated.invalidated(null);
            }

            return time;
        }

        private JoiningTextFlow getDetails(LogEntry entry) {
            final Text activityName = new Text();
            activityName.textProperty().bind(CustomBindings.select(entry.activityProperty(), Activity::nameProperty));
            return new JoiningTextFlow(activityName,
                    entry.whatProperty(),
                    CustomBindings.select(entry.meansOfTransportProperty(), MeansOfTransport::nameProperty));
        }

        private void onMouseClicked(MouseEvent mouseEvent) {
            if (mouseEvent.getClickCount() != 2 || !mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
            new LogEntryDialog(entry).show();
        }

        @Override
        public boolean equals(Object o) {
            //noinspection ObjectComparison
            if (this == o) return true;
            if (o == null || !getClass().equals(o.getClass())) return false;
            ActivityLine that = (ActivityLine) o;
            return entry.equals(that.entry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entry);
        }
    }
}
