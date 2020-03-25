package timelog.model.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;

public class AssociationTableDefinition<A extends ModelObject<A>, B extends ModelObject<B>, T extends Association<A, B>> extends TableDefinition<T> {
    private final String firstColumn;
    private final String secondColumn;
    private final String firstColumnPlaceholder;
    private final String secondColumnPlaceholder;

    public AssociationTableDefinition(String tableName,
                                      String firstColumn, Class<A> firstColumnType,
                                      String secondColumn, Class<B> secondColumnType) {
        this(new AssociationTableDefinition<A, B, T>(tableName,
                        firstColumn, TableDefinition.ColumnType.getForeignKeyColumn(firstColumnType), Association::getFirst),
                secondColumn, TableDefinition.ColumnType.getForeignKeyColumn(secondColumnType), Association::getSecond
        );
    }

    protected <C> AssociationTableDefinition(AssociationTableDefinition<A, B, T> extend, String anotherColumn, ColumnType<C> type, Function<T, C> getter) {
        super(extend, anotherColumn, type, getter);
        assert getNumberOfColumns() == 2;

        this.firstColumnPlaceholder = extend.firstColumnPlaceholder;
        this.firstColumn = extend.firstColumn;
        this.secondColumnPlaceholder = anotherColumn + "=?";
        this.secondColumn = anotherColumn;
    }

    protected <C> AssociationTableDefinition(String tableName, String firstColumn, ColumnType<C> type, Function<T, C> getter) {
        super(tableName, firstColumn, type, getter);
        this.firstColumnPlaceholder = firstColumn + "=?";
        this.firstColumn = firstColumn;
        this.secondColumnPlaceholder = "";
        this.secondColumn = "";
    }

    @Override
    public String getDeleteSQL() {
        return "DELETE FROM " + tableName + " WHERE " + firstColumnPlaceholder + " AND " + secondColumnPlaceholder;
    }

    public String getSelectAllOfFirstSQL() {
        return getBaseSelectSQL() + " WHERE " + firstColumnPlaceholder;
    }

    public void setSQLParams(PreparedStatement statement, A first, B second) throws SQLException {
        statement.setInt(1, first.getId());
        statement.setInt(2, second.getId());
    }

    public String getFirstColumnName() {
        return firstColumn;
    }

    public String getSecondColumnName() {
        return secondColumn;
    }


}
