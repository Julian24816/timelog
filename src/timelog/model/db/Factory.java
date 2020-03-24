package timelog.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Factory<T extends DatabaseObject> {
    private static Consumer<Throwable> errorHandler = Throwable::printStackTrace;
    private final TableDefinition<T> definition;
    private final ResultViewConverter<T> getItemFromView;

    protected Factory(TableDefinition<T> definition, ResultViewConverter<T> getItemFromView) {
        this.definition = definition;
        this.getItemFromView = getItemFromView;
    }

    public static void setErrorHandler(Consumer<Throwable> errorHandler) {
        Factory.errorHandler = Objects.requireNonNull(errorHandler);
    }

    public boolean update(T obj) {
        final String sql = definition.getUpdateSQL();
        try (final Connection connection = Database.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            definition.setSQLParams(statement, obj);
            statement.setInt(definition.getNumberOfColumns() + 1, obj.getId());
            final int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
        return false;
    }

    public T createNew(Object... params) {
        final String sql = definition.getCreateSQL();
        try (final Connection connection = Database.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            definition.setSQLParams(statement, params);
            statement.execute();
            try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    return getForId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
        return null;
    }

    public Collection<T> getAll() {
        return select(this::selectAll, null, 0, null);
    }

    protected final <R> R select(Selector<R> selector, String where, int params, ParameterSetter paramSetter) {
        if (params < 0) throw new IllegalArgumentException("params must be >= 0");
        String sql = definition.getBaseSelect();
        if (where != null && !where.isEmpty()) sql += " WHERE " + where;
        try (final Connection connection = Database.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= params; i++) paramSetter.set(statement, i);
            try (final ResultSet resultSet = statement.executeQuery()) {
                return selector.select(resultSet);
            }
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
        return null;
    }

    protected final Collection<T> selectAll(ResultSet resultSet) throws SQLException {
        final ResultView view = new ResultView(resultSet);
        final List<T> list = new LinkedList<>();
        while (resultSet.next()) list.add(getItemFromView.get(view));
        return list;
    }

    public T getForId(int id) {
        return select(this::selectFirst, "id=?", 1, (preparedStatement, param) -> preparedStatement.setInt(param, id));
    }

    protected final T selectFirst(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) return getItemFromView.get(new ResultView(resultSet));
        return null;
    }

    protected interface Selector<R> {
        R select(ResultSet resultSet) throws SQLException;
    }
}
