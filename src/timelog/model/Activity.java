package timelog.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import timelog.model.db.DatabaseObject;
import timelog.model.db.Factory;
import timelog.model.db.TableDefinition;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Activity implements DatabaseObject<Activity> {
    public static final ActivityFactory FACTORY = new ActivityFactory();

    private final int id;
    private int parentId;
    private final StringProperty name = new SimpleStringProperty(this, "name");

    private Activity(int id, Activity parent, String name) {
        this.id = id;
        this.parentId = parent == null ? 0 : parent.getId();
        this.name.setValue(name);
    }

    @Override
    public int getId() {
        return id;
    }

    private int getDepth() {
        if (id == 0) return 0;
        else return getParent().getDepth() + 1;
    }

    public Activity getParent() {
        return FACTORY.getForId(parentId);
    }

    public void setParent(Activity parent) {
        parentId = Objects.requireNonNull(parent).getId();
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
        return "Activity{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", name=" + name.get() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        //noinspection ObjectComparison
        if (this == o) return true;
        if (o == null || !getClass().equals(o.getClass())) return false;
        Activity activity = (Activity) o;
        return id == activity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Activity o) {
        if (this.equals(o)) return 0;
        if (getDepth() < o.getDepth()) return -o.compareTo(this);
        if (getDepth() == o.getDepth()) {
            final int parents = getParent().compareTo(o.getParent());
            if (parents == 0) return getName().compareTo(o.getName());
            return parents;
        }
        // getDepth() > o.getDepth()
        final int parent = getParent().compareTo(o);
        if (parent == 0) return 1;
        return parent;
    }

    @Override
    public String getDisplayName() {
        if (id == 0) return "(" + name.get() + ")";
        return "- ".repeat(getDepth() - 1) + name.get();
    }

    public static class ActivityFactory extends Factory<Activity> {
        private final Activity root;
        private Map<Integer, Activity> activityMap = new HashMap<>();

        private ActivityFactory() {
            super(
                    new TableDefinition<>("activity",
                            "parent", TableDefinition.ColumnType.DATABASE_OBJECT, Activity::getParent)
                            .and("name", TableDefinition.ColumnType.STRING, Activity::getName),
                    view -> new Activity(
                            view.getInt("id"),
                            FACTORY.getForId(view.getInt("parent")),
                            view.getString("name")
                    )
            );
            final String name = select(resultSet -> resultSet.next() ? resultSet.getString(3) : null, "id=0");
            root = new Activity(0, null, name == null ? "Activity" : name);
            if (name == null)
                execute("INSERT INTO activity VALUES (0, 0, 'Activity');", PreparedStatement::execute, null);
        }

        @Override
        public Activity getForId(int id) {
            if (id == 0) return root;
            getAll();
            if (!activityMap.containsKey(id)) {
                Activity activity = super.getForId(id);
                if (activity != null) putActivity(activity);
            }
            return activityMap.get(id);
        }

        @Override
        public Collection<Activity> getAll() {
            if (activityMap.isEmpty()) {
                super.getAll().forEach(this::putActivity);
            }
            return activityMap.values();
        }

        private void putActivity(Activity activity) {
            activityMap.put(activity.getId(), activity);
        }

        @Override
        public Activity createNew(Object... params) {
            final Activity activity = super.createNew(params);
            putActivity(activity);
            return activity;
        }

        @Override
        public boolean update(Activity obj) {
            activityMap.replace(obj.getId(), obj);
            return super.update(obj);
        }

    }
}
