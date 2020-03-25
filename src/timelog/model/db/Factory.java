package timelog.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Factory<T extends DatabaseObject> {
    protected final SQLFunction<ResultView, T> resultConverter;
    private final TableDefinition<T> definition;

    protected Factory(SQLFunction<ResultView, T> resultConverter, TableDefinition<T> definition) {
        this.resultConverter = resultConverter;
        this.definition = definition;
    }

    protected final <R> R selectWhere(SQLFunction<ResultSet, R> selector, String where, int params, SQLBiConsumer<PreparedStatement, Integer> paramSetter) {
        if (params < 0) throw new IllegalArgumentException("params must be >= 0");
        String sql = definition.getBaseSelectSQL();
        if (where != null && !where.isEmpty()) sql += " WHERE " + where;
        return Database.execute(sql, statement -> {
            for (int i = 1; i <= params; i++) paramSetter.accept(statement, i);
            try (final ResultSet resultSet = statement.executeQuery()) {
                return selector.apply(resultSet);
            }
        }, null);
    }

    protected final T selectFirst(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) return resultConverter.apply(new ResultView(resultSet));
        return null;
    }

    protected final Collection<T> selectAll(ResultSet resultSet) throws SQLException {
        final ResultView view = new ResultView(resultSet);
        final List<T> list = new LinkedList<>();
        while (resultSet.next()) list.add(resultConverter.apply(view));
        return list;
    }

    protected interface SQLBiConsumer<T, U> {
        void accept(T first, U second) throws SQLException;
    }
}
