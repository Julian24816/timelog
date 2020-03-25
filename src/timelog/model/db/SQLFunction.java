package timelog.model.db;

import java.sql.SQLException;

public interface SQLFunction<P, R> {
    R apply(P value) throws SQLException;
}
