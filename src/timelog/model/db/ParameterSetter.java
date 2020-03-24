package timelog.model.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ParameterSetter {
    void set(PreparedStatement preparedStatement, int param) throws SQLException;
}
