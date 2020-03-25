package timelog.model.db;

import java.sql.ResultSet;
import java.util.Collection;

public class ModelFactory<T extends ModelObject<T>> extends Factory<T> {

    private final ModelTableDefinition<T> modelTableDefinition;

    protected ModelFactory(SQLFunction<ResultView, T> getItemFromView, ModelTableDefinition<T> definition) {
        super(getItemFromView, definition);
        modelTableDefinition = definition;
    }

    public boolean update(T obj) {
        final String sql = modelTableDefinition.getUpdateSQL();
        return Database.execute(sql, statement -> {
            modelTableDefinition.setSQLParams(statement, obj);
            statement.setInt(modelTableDefinition.getNumberOfColumns() + 1, obj.getId());
            final int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }, false);
    }

    public T createNew(Object... params) {
        final String sql = modelTableDefinition.getInsertSQL();
        return Database.execute(sql, statement -> {
            modelTableDefinition.setSQLParams(statement, params);
            statement.execute();
            try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) return getForId(generatedKeys.getInt(1));
            }
            return null;
        }, null);
    }

    public T getForId(int id) {
        return selectWhere(this::selectFirst, "id=?", 1, (preparedStatement, param) -> preparedStatement.setInt(param, id));
    }


    public Collection<T> getAll() {
        return selectWhere(this::selectAll, null);
    }

    protected final <R> R selectWhere(SQLFunction<ResultSet, R> selector, String where) {
        return selectWhere(selector, where, 0, null);
    }
}
