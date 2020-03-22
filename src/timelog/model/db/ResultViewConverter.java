package timelog.model.db;

import java.sql.SQLException;

public interface ResultViewConverter<T> {
    T get(ResultView view) throws SQLException;
}
