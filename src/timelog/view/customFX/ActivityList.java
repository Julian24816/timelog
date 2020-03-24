package timelog.view.customFX;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import timelog.model.LogEntry;

import java.util.Objects;

public class ActivityList extends ScrollPane {

    private final VBox vBox = new VBox();
    private final Text placeholder = new Text("no activities on this day");

    private final ObservableList<LogEntry> entries = FXCollections.observableArrayList();

    public ActivityList() {
        super();
        VBox.setMargin(placeholder, new Insets(20, 20, 20, 20));
        vBox.getChildren().add(placeholder);
        setContent(vBox);
        setFitToWidth(true);

        entries.addListener(this::onListChanged);
    }

    private void onListChanged(ListChangeListener.Change<? extends LogEntry> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be permutated"));
            } else if (c.wasUpdated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be updated"));
            } else {
                for (LogEntry added : c.getAddedSubList()) ActivityList.this.addEntry(added);
                for (LogEntry removed : c.getRemoved()) ActivityList.this.removeEntry(removed);
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
        private final LogEntry entry;

        private ActivityLine(LogEntry entry) {
            super(10);
            this.entry = entry;

            final TimeText start = new TimeText();
            start.valueProperty().bind(entry.startProperty());
            final TimeText end = new TimeText();
            end.valueProperty().bind(entry.endProperty());
            final Text what = new Text();
            what.textProperty().bind(entry.activityProperty());
            what.setFont(new Font(20));

            getChildren().addAll(new VBox(start, end), what);
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
