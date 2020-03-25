package timelog.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import timelog.model.db.DatabaseObject;
import timelog.model.db.Factory;
import timelog.model.db.TableDefinition;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;

public final class LogEntry implements DatabaseObject<LogEntry> {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final LogEntryFactory FACTORY = new LogEntryFactory();

    private final int id;
    private final ObjectProperty<Activity> activity = new SimpleObjectProperty<>(this, "activity");
    private final StringProperty what = new SimpleStringProperty(this, "what");
    private final ObjectProperty<LocalDateTime> start = new SimpleObjectProperty<>(this, "start");
    private final ObjectProperty<LocalDateTime> end = new SimpleObjectProperty<>(this, "end");

    private LogEntry(int id, Activity activity, String what, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.activity.setValue(Objects.requireNonNull(activity));
        this.what.setValue(Objects.requireNonNull(what));
        this.start.setValue(Objects.requireNonNull(start));
        this.end.setValue(end);
    }

    @Override
    public int getId() {
        return id;
    }

    public ObjectProperty<Activity> activityProperty() {
        return activity;
    }

    public Activity getActivity() {
        return activity.get();
    }

    public void setActivity(Activity value) {
        activity.setValue(value);
    }

    public StringProperty whatProperty() {
        return what;
    }

    public String getWhat() {
        return what.getValue();
    }

    public void setWhat(String value) {
        what.setValue(value);
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

    @Override
    public String toString() {
        String result = "LogEntry{" +
                "id=" + id +
                ", activity=" + what.get() +
                ", start=" + FORMATTER.format(start.get());
        if (end.get() == null) result += ", end=null";
        else result += ", end=" + FORMATTER.format(end.get());
        result += '}';
        return result;
    }

    @Override
    public boolean equals(Object o) {
        //noinspection ObjectComparison
        if (this == o) return true;
        if (o == null || !getClass().equals(o.getClass())) return false;
        LogEntry logEntry = (LogEntry) o;
        return id == logEntry.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(LogEntry o) {
        if (id == o.id) return 0;
        if (getEnd() == null && o.getEnd() == null) return Integer.compare(id, o.id);
        if (getEnd() == null) return 1;
        if (o.getEnd() == null) return -1;
        return getEnd().compareTo(o.getEnd());
    }

    public LocalDateTime getEnd() {
        return end.getValue();
    }

    public void setEnd(LocalDateTime value) {
        end.setValue(value);
    }

    public static class LogEntryFactory extends Factory<LogEntry> {
        private LogEntryFactory() {
            super(
                    new TableDefinition<>("log",
                            "activity", TableDefinition.ColumnType.DATABASE_OBJECT, LogEntry::getActivity)
                            .and("what", TableDefinition.ColumnType.STRING, LogEntry::getWhat)
                            .and("start", TableDefinition.ColumnType.TIMESTAMP, LogEntry::getStart)
                            .and("end", TableDefinition.ColumnType.TIMESTAMP, LogEntry::getEnd),
                    view -> new LogEntry(
                            view.getInt("id"),
                            Activity.FACTORY.getForId(view.getInt("activity")),
                            view.getString("what"),
                            view.getDateTime("start"),
                            view.getDateTime("end")
                    )
            );
        }

        public LogEntry getUnfinishedEntry() {
            return select(this::selectFirst, "end IS NULL", 0, null);
        }

        public Collection<LogEntry> getAllFinishedOn(LocalDate date) {
            return select(this::selectAll, "end >= ? AND end < ?", 2, (preparedStatement, param) -> {
                if (param == 1) preparedStatement.setTimestamp(param, Timestamp.valueOf(date.atTime(0, 0)));
                if (param == 2)
                    preparedStatement.setTimestamp(param, Timestamp.valueOf(date.plus(1, ChronoUnit.DAYS).atTime(0, 0)));
            });
        }
    }
}
