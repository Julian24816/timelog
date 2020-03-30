package timelog.view;

import javafx.beans.binding.Bindings;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import timelog.model.Activity;
import timelog.model.LogEntry;
import timelog.view.customFX.CustomBindings;
import timelog.view.customFX.ErrorAlert;
import timelog.view.customFX.TimeText;
import timelog.view.edit.LogEntryDialog;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

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
        public static final int EDGE_TIME_HEIGHTS = 30;
        private final LogEntry entry;

        private ActivityLine(LogEntry entry) {
            super(10);
            this.entry = entry;
            getChildren().addAll(getTimeVBox(entry), getDetailsVBox(entry));
            setOnMouseClicked(this::onMouseClicked);
            backgroundProperty().bind(CustomBindings.apply(
                    CustomBindings.select(entry.activityProperty(), Activity::colorProperty),
                    color -> new Background(new BackgroundFill(Color.valueOf(color), null, null))));
        }

        private VBox getTimeVBox(LogEntry entry) {
            final TimeText start = new TimeText();
            start.valueProperty().bind(entry.startProperty());

            final VLineTo vLineTo = new VLineTo(getLineHeight());
            final Path line = new Path(new MoveTo(0, 0), vLineTo);
            entry.startProperty().addListener(observable -> vLineTo.setY(getLineHeight()));
            entry.endProperty().addListener(observable -> vLineTo.setY(getLineHeight()));

            final TimeText end = new TimeText();
            end.valueProperty().bind(entry.endProperty());

            final VBox time = new VBox(start, line, end);
            time.setAlignment(Pos.CENTER);
            return time;
        }

        private VBox getDetailsVBox(LogEntry entry) {
            final Text activityName = new Text();
            activityName.textProperty().bind(Bindings.select(entry, "activity", "name"));
            activityName.setFont(new Font(16));

            final Text what = new Text();
            what.textProperty().bind(entry.whatProperty());

            final Text transport = new Text();
            transport.textProperty().bind(CustomBindings.ifNull(entry.meansOfTransportProperty(),
                    p -> ", " + p.getDisplayName(), ""));

            return new VBox(activityName, new TextFlow(what, transport));
        }

        private void onMouseClicked(MouseEvent mouseEvent) {
            if (mouseEvent.getClickCount() != 2 || !mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
            new LogEntryDialog(entry).show();
        }

        private long getLineHeight() {
            if (entry.getEnd() == null) return 0;
            final long size = entry.getStart().until(entry.getEnd(), ChronoUnit.MINUTES);
            return Math.max(EDGE_TIME_HEIGHTS + 2, Math.min(240, size) / 2) - EDGE_TIME_HEIGHTS;
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
