package timelog.model;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * holds a connection pool
 */
public final class Database {
    private static BasicDataSource dataSource;

    private Database() {
    }

    public static void init(String url, String user, String password) {
        dataSource = new BasicDataSource();
        dataSource.setMinIdle(1);
        dataSource.setMaxIdle(10);
        dataSource.setMaxOpenPreparedStatements(100);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
    }

    public static void execFile(Path filename) throws SQLException, IOException {
        try (final Connection connection = getConnection()) {
            for (String sql : Files.readString(filename).split(";"))
                try (final Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
        }
    }

    static Connection getConnection() throws SQLException {
        if (dataSource == null) throw new IllegalStateException("dataSource not initialized");
        return dataSource.getConnection();
    }
}
