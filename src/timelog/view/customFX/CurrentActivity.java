package timelog.view.customFX;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import timelog.model.LogEntry;

import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class CurrentActivity extends VBox {
    private final Timer timer = new Timer(true);
    private final Text activityName = new Text("No Current Activity");
    private final Text duration = new Text("--:--");
    private final Text startTime = new Text("--:--");
    private final ObjectProperty<LogEntry> activity = new SimpleObjectProperty<>(this, "current activity") {
        @Override
        protected void invalidated() {
            if (getValue() == null) {
                activityName.setText("No Current Activity");
                startTime.setText("--:--");
                duration.setText("--:--");
            } else {
                activityName.setText(getValue().getActivity());
                startTime.setText(TimeTextField.TIME_FORMATTER.format(getValue().getStartTime()));
                duration.setText(TimeTextField.TIME_FORMATTER.format(getValue().getDuration().addTo(LocalTime.MIN)));
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (getValue() == null) cancel();
                        else duration.setText(TimeTextField.TIME_FORMATTER.format(getValue().getDuration().addTo(LocalTime.MIN)));
                    }
                }, 1000, 1000);
            }
        }
    };

    public CurrentActivity(LogEntry activity) {
        this.activity.setValue(activity);

        final Font font = new Font(20);
        activityName.setFont(font);
        duration.setFont(font);

        Label title = new Label("Current Activity, ");
        title.setLabelFor(activityName);

        Label label = new Label("started at ");
        label.setLabelFor(startTime);

        Region spacer = new Region();
        spacer.setPrefWidth(30);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().add(new HBox(title, label, startTime));
        getChildren().add(new HBox(activityName, spacer, duration));
    }

    public ObjectProperty<LogEntry> activityProperty() {
        return activity;
    }

    public LogEntry getActivity() {
        return activity.getValue();
    }

    public void setActivity(LogEntry value) {
        activity.setValue(value);
    }
}
