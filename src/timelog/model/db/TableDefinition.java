package timelog.model.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class TableDefinition<T extends DatabaseObject> {
    protected final String tableName;
    protected final List<ColumnType<?>> types;
    protected final List<Function<T, ?>> getters;
    private final String columnNames;
    private final String insertPlaceholders;
    private final int numberOfColumns;

    protected <C> TableDefinition(String tableName, String firstColumn, ColumnType<C> type, Function<T, C> getter) {
        this.tableName = tableName;

        this.columnNames = firstColumn;
        this.insertPlaceholders = "?";
        this.numberOfColumns = 1;

        this.types = List.of(type);
        this.getters = List.of(getter);
    }

    protected <C> TableDefinition(TableDefinition<T> extend, String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        tableName = extend.tableName;

        columnNames = extend.columnNames + "," + anotherColumn;
        insertPlaceholders = extend.insertPlaceholders + ",?";
        numberOfColumns = extend.numberOfColumns + 1;

        types = new ArrayList<>(extend.types);
        types.add(type);
        getters = new ArrayList<>(extend.getters);
        getters.add(getter);
    }

    String getBaseSelectSQL() {
        return "SELECT " + columnNames + " FROM " + tableName;
    }

    String getInsertSQL() {
        return "INSERT INTO " + tableName + "(" + columnNames + ") VALUES (" + insertPlaceholders + ")";
    }

    void setSQLParams(PreparedStatement statement, Object[] params) throws SQLException {
        if (params.length != getNumberOfColumns()) throw new IllegalArgumentException("wrong number of parameters");
        int paramNumber = 0;
        for (ColumnType<?> type : types) {
            Object param = params[paramNumber];
            type.apply(statement, paramNumber + 1, param);
            paramNumber++;
        }
    }

    int getNumberOfColumns() {
        return numberOfColumns;
    }

    void setSQLParams(PreparedStatement statement, T obj) throws SQLException {
        int getter = 0;
        for (ColumnType<?> type : types) {
            type.apply(statement, getter + 1, getters.get(getter).apply(obj));
            getter++;
        }
    }

    public abstract String getDeleteSQL();

    public static class ColumnType<T> {
        public static final ColumnType<Integer> INTEGER = new ColumnType<>(
                PreparedStatement::setInt,
                param -> tryCast(Integer.class, param));
        public static final ColumnType<String> STRING = new ColumnType<>(
                PreparedStatement::setString,
                param -> tryCast(String.class, param));
        public static final ColumnType<LocalDateTime> TIMESTAMP = new ColumnType<>(
                (statement, index, value) -> statement.setTimestamp(index,
                        value == null ? null : Timestamp.valueOf(value)),
                param -> tryCast(LocalDateTime.class, param));

        private final Function<Object, T> converter;
        private final StatementApplier<T> applier;

        private ColumnType(StatementApplier<T> applier, Function<Object, T> converter) {
            this.converter = converter;
            this.applier = applier;
        }

        public static <C extends ModelObject<C>> ColumnType<C> getForeignKeyColumn(Class<C> clazz) {
            return new ColumnType<>(
                    (statement, index, value) -> {
                        if (value == null) statement.setObject(index, null);
                        else statement.setInt(index, value.getId());
                    },
                    param -> tryCast(clazz, param));
        }

        private static <T> T tryCast(Class<T> clazz, Object param) {
            try {
                return clazz.cast(param);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("required " + clazz.getName() + " but was " + param.getClass().getName());
            }
        }

        public void apply(PreparedStatement statement, int index, Object param) throws SQLException {
            applier.apply(statement, index, converter.apply(param));
        }

        private interface StatementApplier<T> {
            void apply(PreparedStatement statement, int index, T value) throws SQLException;
        }
    }
}
