package timelog.statistic;

import timelog.model.LogEntry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

public final class Statistics {
    private Statistics() {
    }

    public static ActivityStatistic activitiesToday() {
        return ActivityStatistic.of(entriesToday());
    }

    private static Collection<LogEntry> entriesToday() {
        return LogEntry.FACTORY.getAllFinishedOn(LocalDate.now());
    }

    public static QualityTimeStatistic qualityTimeToday() {
        return QualityTimeStatistic.of(entriesToday());
    }

    public static ActivityStatistic activitiesThisWeek() {
        return ActivityStatistic.of(entriesLastWeek());
    }

    private static Collection<LogEntry> entriesLastWeek() {
        return LogEntry.FACTORY.getAllFinishedBetween(
                LocalDate.now().minus(6, ChronoUnit.DAYS).atTime(0, 0),
                LocalDate.now().plus(1, ChronoUnit.DAYS).atTime(0, 0)
        );
    }

    public static QualityTimeStatistic qualityTimeThisWeek() {
        return QualityTimeStatistic.of(entriesLastWeek());
    }
}
