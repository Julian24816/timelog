package timelog.statistic;

import timelog.model.LogEntry;
import timelog.model.Person;
import timelog.model.QualityTime;

import java.time.Duration;
import java.util.Collection;

public class QualityTimeStatistic extends Statistic<Person, Duration> {
    private QualityTimeStatistic(Person person) {
        super(person, person == null ? "People" : person.getName(), DurationDatum::new);
    }

    public static QualityTimeStatistic of(Collection<LogEntry> entries) {
        final QualityTimeStatistic statistic = new QualityTimeStatistic(null);
        entries.forEach(logEntry ->
                QualityTime.FACTORY.getAll(logEntry).forEach(qualityTime ->
                        statistic.add(qualityTime.getSecond(), DurationDatum.of(logEntry)))
        );
        return statistic;
    }

    @Override
    protected Statistic<Person, Duration> newStatistic(Person key) {
        return new QualityTimeStatistic(key);
    }
}
