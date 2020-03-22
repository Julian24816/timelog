package timelog.model;

import java.sql.SQLException;

public interface ResultViewConverter<T> {
    T get(ResultView view) throws SQLException;
}
