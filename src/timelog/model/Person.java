package timelog.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import timelog.model.db.ModelFactory;
import timelog.model.db.ModelObject;
import timelog.model.db.ModelTableDefinition;
import timelog.model.db.TableDefinition;

import java.util.Objects;

public final class Person extends ModelObject<Person> {
    public static final PersonFactory FACTORY = new PersonFactory();

    private final StringProperty name = new SimpleStringProperty(this, "name");

    private Person(int id, String name) {
        super(id);
        this.name.setValue(Objects.requireNonNull(name));
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.setValue(value);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + getId() +
                ", name=" + name.get() +
                '}';
    }

    @Override
    public int compareTo(Person o) {
        return getDisplayName().compareTo(getDisplayName());
    }

    @Override
    public String getDisplayName() {
        return name.get();
    }

    public static final class PersonFactory extends ModelFactory<Person> {
        private PersonFactory() {
            super(view -> new Person(
                            view.getInt("id"),
                            view.getString("name")
                    ),
                    new ModelTableDefinition<Person>("person")
                            .withColumn("name", TableDefinition.ColumnType.STRING, Person::getName)
            );
        }
    }
}