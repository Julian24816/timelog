package timelog.model;

import timelog.model.db.Association;
import timelog.model.db.AssociationFactory;
import timelog.model.db.AssociationTableDefinition;

public final class QualityTime extends Association<LogEntry, Person> {
    public static final QualityTimeFactory FACTORY = new QualityTimeFactory();

    private QualityTime(LogEntry logEntry, Person person) {
        super(logEntry, person);
    }

    public static final class QualityTimeFactory extends AssociationFactory<LogEntry, Person, QualityTime> {

        private QualityTimeFactory() {
            super(QualityTime::new, Person.FACTORY, new AssociationTableDefinition<>("qualityTime",
                    "logEntry", LogEntry.class, "person", Person.class)
            );
        }
    }
}
