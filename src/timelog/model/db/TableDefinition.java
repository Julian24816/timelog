package timelog.model.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public final class TableDefinition<T extends DatabaseObject> {
    private final String tableName;
    private final String columnNames;
    private final String insertPlaceholders;
    private final String columnNamesWithPlaceholders;
    private final int numberOfColumns;
    private final List<ColumnType<?>> types;
    private final List<Function<T, ?>> getters;

    /**
     * @param <C>         the column type
     * @param tableName   the name of the table in the database corresponding to T
     * @param firstColumn the name of the first column (not the ID column)
     * @param type        the type of the first column
     * @param getter      a getter for the corresponding field of T
     */
    public <C> TableDefinition(String tableName, String firstColumn, ColumnType<C> type, Function<T, C> getter) {
        this.tableName = tableName;
        this.columnNames = firstColumn;
        this.insertPlaceholders = "?";
        this.columnNamesWithPlaceholders = firstColumn + "=?";
        this.numberOfColumns = 1;
        this.types = List.of(type);
        this.getters = List.of(getter);
    }

    private <C> TableDefinition(TableDefinition<T> extend, String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        this.tableName = extend.tableName;
        this.columnNames = extend.columnNames + "," + anotherColumn;
        this.insertPlaceholders = extend.insertPlaceholders + ",?";
        this.columnNamesWithPlaceholders = extend.columnNamesWithPlaceholders + "," + anotherColumn + "=?";
        this.numberOfColumns = extend.numberOfColumns + 1;
        this.types = new LinkedList<>(extend.types);
        this.types.add(type);
        this.getters = new ArrayList<>(extend.getters);
        this.getters.add(getter);
    }

    /**
     * @param <C>           the column type
     * @param anotherColumn the name of the additional column
     * @param type          the class of the additional column
     * @param getter        a getter for the corresponding field of T
     * @return a new TableDefinition with the additional column
     */
    public <C> TableDefinition<T> and(String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        return new TableDefinition<>(this, anotherColumn, type, getter);
    }

    String getAllSQL() {
        return "SELECT id," + columnNames + " FROM " + tableName;
    }

    String getForIdSQL() {
        return "SELECT * FROM " + tableName + " WHERE id=?";
    }

    String getCreateSQL() {
        return "INSERT INTO " + tableName + "(" + columnNames + ") VALUES (" + insertPlaceholders + ")";
    }

    void setSQLParams(PreparedStatement statement, Object[] params) throws SQLException {
        if (params.length != types.size()) throw new IllegalArgumentException("wrong number of parameters");
        int i = 0;
        for (ColumnType<?> type : types) {
            Object param = params[i];
            type.apply(statement, i + 1, param);
            i++;
        }
    }

    String getUpdateSQL() {
        return "UPDATE " + tableName + " SET " + columnNamesWithPlaceholders + " WHERE id=?";
    }

    int getNumberOfColumns() {
        return numberOfColumns;
    }

    void setSQLParams(PreparedStatement statement, T obj) throws SQLException {
        int i = 0;
        for (ColumnType<?> type : types) {
            type.apply(statement, i + 1, getters.get(i).apply(obj));
            i++;
        }
    }

    public static class ColumnType<T> {
        public static final ColumnType<Integer> INTEGER = new ColumnType<>(
                PreparedStatement::setInt,
                param -> {
                    try {
                        return (Integer) param;
                    } catch (ClassCastException e) {
                        throw new IllegalArgumentException("required int but was " + param.getClass().getName());
                    }
                });

        public static final ColumnType<String> STRING = new ColumnType<>(
                PreparedStatement::setString,
                param -> {
                    try {
                        return (String) param;
                    } catch (ClassCastException e) {
                        throw new IllegalArgumentException("required String but was " + param.getClass().getName());
                    }
                });

        public static final ColumnType<LocalDateTime> TIMESTAMP = new ColumnType<>(
                (statement, index, value) -> statement.setTimestamp(index,
                        value == null ? null : Timestamp.valueOf(value)),
                param -> {
                    try {
                        return (LocalDateTime) param;
                    } catch (ClassCastException e) {
                        throw new IllegalArgumentException("required LocalDateTime but was " + param.getClass().getName());
                    }
                });

        public static final ColumnType<DatabaseObject> DATABASE_OBJECT = new ColumnType<>(
                (statement, index, value) -> statement.setInt(index, value.getId()),
                param -> {
                    try {
                        return (DatabaseObject) param;
                    } catch (ClassCastException e) {
                        throw new IllegalArgumentException("required LocalDateTime but was " + param.getClass().getName());
                    }
                });

        private final Function<Object, T> converter;
        private final StatementApplier<T> applier;

        private ColumnType(StatementApplier<T> applier, Function<Object, T> converter) {
            this.converter = converter;
            this.applier = applier;
        }

        public void apply(PreparedStatement statement, int index, Object param) throws SQLException {
            applier.apply(statement, index, converter.apply(param));
        }

        private interface StatementApplier<T> {
            void apply(PreparedStatement statement, int index, T value) throws SQLException;
        }
    }
}
