package timelog.model.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;

public final class ModelTableDefinition<T extends ModelObject<T>> extends TableDefinition<T> {

    private final String columnNamesWithPlaceholders;
    private final String columnNames;
    private final String insertPlaceholders;

    public ModelTableDefinition(String tableName) {
        super(tableName, "id", ColumnType.INTEGER, ModelObject::getId);
        this.columnNamesWithPlaceholders = "";
        this.columnNames = "";
        this.insertPlaceholders = "";
    }

    private <C> ModelTableDefinition(ModelTableDefinition<T> extend, String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        super(extend, anotherColumn, type, getter);
        if (getNumberOfColumns() == 1) {
            columnNamesWithPlaceholders = anotherColumn + "=?";
            columnNames = anotherColumn;
            insertPlaceholders = "?";
        } else {
            columnNamesWithPlaceholders = extend.columnNamesWithPlaceholders + "," + anotherColumn + "=?";
            columnNames = extend.columnNames + "," + anotherColumn;
            insertPlaceholders = extend.insertPlaceholders + ",?";
        }
    }

    public <C> ModelTableDefinition<T> withColumn(String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        return new ModelTableDefinition<>(this, anotherColumn, type, getter);
    }

    @Override
    String getInsertSQL() {
        return "INSERT INTO " + tableName + "(" + columnNames + ") VALUES (" + insertPlaceholders + ")";
    }

    @Override
    void setSQLParams(PreparedStatement statement, Object[] params) throws SQLException {
        if (params.length != getNumberOfColumns()) throw new IllegalArgumentException("wrong number of parameters");
        for (int columnNumber = 1; columnNumber < types.size(); columnNumber++) {
            ColumnType<?> type = types.get(columnNumber);
            Object param = params[columnNumber - 1];
            type.apply(statement, columnNumber, param);
        }
    }

    @Override
    int getNumberOfColumns() {
        return super.getNumberOfColumns() - 1;
    }

    @Override
    void setSQLParams(PreparedStatement statement, T obj) throws SQLException {
        for (int columnNumber = 1; columnNumber < types.size(); columnNumber++) {
            ColumnType<?> type = types.get(columnNumber);
            type.apply(statement, columnNumber, getters.get(columnNumber).apply(obj));
        }
    }

    @Override
    public String getDeleteSQL() {
        //TODO implement drop
        return null;
    }

    String getUpdateSQL() {
        return "UPDATE " + tableName + " SET " + columnNamesWithPlaceholders + " WHERE id=?";
    }
}
