package timelog.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ResultView {
    private final Map<String, Integer> map = new HashMap<>();
    private final ResultSet resultSet;

    public ResultView(ResultSet resultSet) {
        this.resultSet = Objects.requireNonNull(resultSet);
    }

    public String getString(String key) throws SQLException {
        return resultSet.getString(getIndex(key));
    }

    private int getIndex(String key) throws SQLException {
        if (!map.containsKey(key)) map.put(key, resultSet.findColumn(key));
        return map.get(key);
    }

    public int getInt(String key) throws SQLException {
        return resultSet.getInt(getIndex(key));
    }

    public LocalDateTime getDateTime(String key) throws SQLException {
        final Timestamp timestamp = resultSet.getTimestamp(getIndex(key));
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
