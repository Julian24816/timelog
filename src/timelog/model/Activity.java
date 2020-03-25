package timelog.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import timelog.model.db.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Activity extends ModelObject<Activity> {
    public static final ActivityFactory FACTORY = new ActivityFactory();
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private int parentId;

    private Activity(int id, int parentId, String name) {
        super(id);
        this.parentId = parentId;
        this.name.setValue(Objects.requireNonNull(name));
    }

    private int getDepth() {
        if (getId() == 0) return 0;
        else return getParent().getDepth() + 1;
    }

    public Activity getParent() {
        return FACTORY.getForId(parentId);
    }

    public void setParent(Activity parent) {
        if (getId() == 0) return;
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
                "id=" + getId() +
                ", parentId=" + parentId +
                ", name=" + name.get() +
                '}';
    }

    @Override
    public int compareTo(Activity o) {
        if (this.equals(o)) return 0;
        if (getDepth() < o.getDepth()) return -o.compareTo(this);
        if (getDepth() == o.getDepth()) {
            final int parents = getParent().compareTo(o.getParent());
            if (parents == 0) {
                final int names = getName().compareTo(o.getName());
                if (names == 0) return Integer.compare(getId(), o.getId());
                return names;
            }
            return parents;
        }
        // else: getDepth() > o.getDepth()
        final int parent = getParent().compareTo(o);
        if (parent == 0) return 1;
        return parent;
    }

    @Override
    public String getDisplayName() {
        if (getId() == 0) return "(" + name.get() + ")";
        return "- ".repeat(getDepth() - 1) + name.get();
    }

    public static final class ActivityFactory extends ModelFactory<Activity> {
        private Map<Integer, Activity> activityMap = new HashMap<>();

        private ActivityFactory() {
            super(view -> new Activity(
                            view.getInt("id"),
                            view.getInt("parent"),
                            view.getString("name")
                    ),
                    new ModelTableDefinition<Activity>("activity")
                            .withColumn("parent", TableDefinition.ColumnType.getForeignKeyColumn(Activity.class), Activity::getParent)
                            .withColumn("name", TableDefinition.ColumnType.STRING, Activity::getName)
            );

            final boolean rootExists = selectWhere(ResultSet::next, "id=0");
            if (!rootExists)
                Database.execute("INSERT INTO activity VALUES (0, 0, 'Activity');", PreparedStatement::execute, null);
        }

        @Override
        public Activity getForId(int id) {
            ensureLoaded();
            if (!activityMap.containsKey(id)) { //just created
                final Activity activity = super.getForId(id);
                if (activity != null) putActivity(activity);
            }
            return activityMap.get(id);
        }

        private void ensureLoaded() {
            if (activityMap.isEmpty()) super.getAll().forEach(this::putActivity);
        }

        private void putActivity(Activity activity) {
            activityMap.put(activity.getId(), activity);
        }

        @Override
        public Collection<Activity> getAll() {
            ensureLoaded();
            return activityMap.values();
        }

        @Override
        public Activity createNew(Object... params) {
            ensureLoaded();
            final Activity activity = super.createNew(params);
            putActivity(activity);
            return activity;
        }

        @Override
        public boolean update(Activity obj) {
            ensureLoaded();
            activityMap.replace(obj.getId(), obj);
            return super.update(obj);
        }

    }
}
