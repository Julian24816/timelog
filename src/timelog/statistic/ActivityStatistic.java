package timelog.statistic;

import timelog.model.Activity;
import timelog.model.LogEntry;

import java.time.Duration;
import java.util.Collection;

public class ActivityStatistic extends Statistic<Activity, Duration> {
    private ActivityStatistic(Activity root) {
        super(root, root.getId() == 0 ? "Activity" : root.getName(), DurationDatum::new);
    }

    static ActivityStatistic of(Collection<LogEntry> entries) {
        final ActivityStatistic statistic = new ActivityStatistic(Activity.FACTORY.getForId(0));
        entries.forEach(logEntry -> statistic.add(logEntry.getActivity(), DurationDatum.of(logEntry)));
        return statistic;
    }

    @Override
    protected Statistic<Activity, Duration> getSubStatistic(Activity activity) {
        if (activity.equals(key)) throw new IllegalArgumentException();
        Activity parent = activity.getParent();
        if (parent.equals(key)) return super.getSubStatistic(activity);
        return getSubStatistic(parent).getSubStatistic(activity);
    }

    @Override
    protected Statistic<Activity, Duration> newStatistic(Activity key) {
        return new ActivityStatistic(key);
    }
}
