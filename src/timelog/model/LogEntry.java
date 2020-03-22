package timelog.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import timelog.model.db.DatabaseObject;
import timelog.model.db.Factory;
import timelog.model.db.TableDefinition;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public final class LogEntry implements DatabaseObject {
    public static final LogEntryFactory FACTORY = new LogEntryFactory();

    private final int id;
    private final StringProperty activity = new SimpleStringProperty(this, "activity");
    private final ObjectProperty<LocalDateTime> start = new SimpleObjectProperty<>(this, "start");
    private final ObjectProperty<LocalDateTime> end = new SimpleObjectProperty<>(this, "end");

    private LogEntry(int id, String activity, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.activity.setValue(Objects.requireNonNull(activity));
        this.start.setValue(Objects.requireNonNull(start));
        this.end.setValue(end);
    }

    public int getId() {
        return id;
    }

    public StringProperty activityProperty() {
        return activity;
    }

    public String getActivity() {
        return activity.getValue();
    }

    public void setActivity(String value) {
        activity.setValue(value);
    }

    public ObjectProperty<LocalDateTime> startProperty() {
        return start;
    }

    public LocalDateTime getStart() {
        return start.getValue();
    }

    public void setStart(LocalDateTime value) {
        start.setValue(value);
    }

    public ObjectProperty<LocalDateTime> endProperty() {
        return end;
    }

    public LocalDateTime getEnd() {
        return end.getValue();
    }

    public void setEnd(LocalDateTime value) {
        end.setValue(value);
    }

    public Duration getDuration() {
        if (end.get() == null) return Duration.between(start.get(), LocalDateTime.now());
        return Duration.between(start.get(), end.get());
    }

    public LocalTime getStartTime() {
        return start.get().toLocalTime();
    }

    public static class LogEntryFactory extends Factory<LogEntry> {
        public LogEntryFactory() {
            super(
                    new TableDefinition<>("log", "activity", TableDefinition.ColumnType.STRING, LogEntry::getActivity)
                            .and("start", TableDefinition.ColumnType.TIMESTAMP, LogEntry::getStart)
                            .and("end", TableDefinition.ColumnType.TIMESTAMP, LogEntry::getEnd),
                    view -> new LogEntry(
                            view.getInt("id"),
                            view.getString("activity"),
                            view.getDateTime("start"),
                            view.getDateTime("end")
                    )
            );
        }

        public LogEntry getUnfinishedEntry() {
            return getFirstWhere("end IS NULL");
        }
    }
}
