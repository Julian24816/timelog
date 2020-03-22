package timelog.model.db;

import java.sql.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Factory<T extends DatabaseObject> {
    private static Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    public static void setErrorHandler(Consumer<Throwable> errorHandler) {
        Factory.errorHandler = Objects.requireNonNull(errorHandler);
    }

    private final TableDefinition<T> definition;
    private final ResultViewConverter<T> getItemFromView;

    protected Factory(TableDefinition<T> definition, ResultViewConverter<T> getItemFromView) {
        this.definition = definition;
        this.getItemFromView = getItemFromView;
    }

    public Collection<T> getAll() {
        final String sql = definition.getAllSQL();
        final List<T> list = new LinkedList<>();
        try (final Connection connection = Database.getConnection();
             final Statement statement = connection.createStatement()) {
            try (final ResultSet resultSet = statement.executeQuery(sql)) {
                final ResultView view = new ResultView(resultSet);
                while (resultSet.next()) list.add(getItemFromView.get(view));
            }
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
        return list;
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

    public T getForId(int id) {
        final String sql = definition.getForIdSQL();
        try (final Connection connection = Database.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (final ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return getItemFromView.get(new ResultView(resultSet));
            }
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
        return null;
    }

    protected T getFirstWhere(String where) {
        final String sql = definition.getAllSQL() + " WHERE " + where;
        try (final Connection connection = Database.getConnection();
             final Statement statement = connection.createStatement()) {
            try (final ResultSet resultSet = statement.executeQuery(sql)) {
                if (resultSet.next()) return getItemFromView.get(new ResultView(resultSet));
            }
        } catch (SQLException e) {
            errorHandler.accept(e);
        }
        return null;
    }

    public boolean save(T obj) {
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
}
