package timelog.statistic;

public interface StatisticalDatum<D> {
    StatisticalDatum<D> plus(StatisticalDatum<D> value);

    D get();

    @Override
    String toString();
}
