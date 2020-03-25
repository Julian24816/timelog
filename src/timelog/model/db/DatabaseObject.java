package timelog.model.db;

public interface DatabaseObject<T extends DatabaseObject<?>> extends Comparable<T> {
    int getId();

    default String getDisplayName() {
        return toString();
    }
}
