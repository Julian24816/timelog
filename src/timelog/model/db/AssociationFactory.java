package timelog.model.db;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.function.BiFunction;

public abstract class AssociationFactory<A extends ModelObject<A>, B extends ModelObject<B>, T extends Association<A, B>> extends Factory<T> {
    private final AssociationTableDefinition<A, B, T> definition;
    private final BiFunction<A, B, T> constructor;

    protected AssociationFactory(BiFunction<A, B, T> constructor, ModelFactory<A> aFactory, ModelFactory<B> bFactory, AssociationTableDefinition<A, B, T> definition) {
        super(view -> constructor.apply(
                aFactory.getForId(view.getInt(definition.getFirstColumnName())),
                bFactory.getForId(view.getInt(definition.getSecondColumnName()))
        ), definition);
        this.definition = definition;
        this.constructor = constructor;
    }

    public Collection<T> getAll(A first) {
        String sql = definition.getSelectAllOfFirstSQL();
        return Database.execute(sql, statement -> {
            statement.setInt(1, first.getId());
            try (final ResultSet resultSet = statement.executeQuery()) {
                return selectAll(resultSet);
            }
        }, null);
    }

    public T create(A first, B second) {
        String sql = definition.getInsertSQL();
        return Database.execute(sql, statement -> {
            definition.setSQLParams(statement, first, second);
            statement.execute();
            return constructor.apply(first, second);
        }, null);
    }

    public boolean delete(T association) {
        String sql = definition.getDeleteSQL();
        return Database.execute(sql, statement -> {
            definition.setSQLParams(statement, association.getFirst(), association.getSecond());
            final int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }, false);
    }
}
