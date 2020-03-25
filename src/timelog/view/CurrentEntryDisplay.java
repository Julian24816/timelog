package timelog.view;

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
import timelog.model.MeansOfTransport;
import timelog.view.customFX.TimeTextField;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class CurrentEntryDisplay extends VBox {
    private final Timer timer = new Timer(true);
    private final Text startTime = new Text("--:--");
    private final Text transport = new Text("");
    private final Text activityName = new Text("No Current Activity");
    private final Text duration = new Text("--:--");
    private final Text activityWhat = new Text("");
    private final ObjectProperty<LogEntry> activity = new SimpleObjectProperty<>(this, "current activity") {
        @Override
        protected void invalidated() {
            if (getValue() == null) {
                startTime.setText("--:--");
                transport.setText("");
                activityName.setText("No Current Activity");
                duration.setText("--:--");
                activityWhat.setText("");
            } else {
                startTime.setText(TimeTextField.TIME_FORMATTER.format(getValue().getStart().toLocalTime()));
                transport.setText(Optional.ofNullable(getValue().getMeansOfTransport()).map(MeansOfTransport::getDisplayName).orElse(""));
                activityName.setText(getValue().getActivity().getName());
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (getValue() == null) cancel();
                        else setDuration();
                    }
                }, 0, 500);
                activityWhat.setText(getValue().getWhat());
            }
        }

        private void setDuration() {
            Duration duration;
            if (getValue().getEnd() == null) duration = Duration.between(getValue().getStart(), LocalDateTime.now());
            else duration = Duration.between(getValue().getEnd(), getValue().getStart());
            CurrentEntryDisplay.this.duration.setText(TimeTextField.TIME_FORMATTER.format(duration.addTo(LocalTime.MIN)));
        }
    };

    public CurrentEntryDisplay(LogEntry activity) {
        this.activity.setValue(activity);

        final Font font = new Font(20);
        activityName.setFont(font);
        duration.setFont(font);

        Label title = new Label("Current Activity, ");
        title.setLabelFor(activityName);

        Label startTimeLabel = new Label("started at ");
        startTimeLabel.setLabelFor(startTime);

        Label transportSeparator = new Label(", ");
        transportSeparator.setLabelFor(transport);

        Region spacer = new Region();
        spacer.setPrefWidth(30);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        activityWhat.wrappingWidthProperty().bind(widthProperty().add(-10));

        final HBox titleLine = new HBox(title, startTimeLabel, startTime);
        if (!transport.getText().isEmpty()) {
            titleLine.getChildren().add(transportSeparator);
            titleLine.getChildren().add(transport);
        }
        transport.textProperty().addListener(observable -> {
            titleLine.getChildren().remove(transportSeparator);
            titleLine.getChildren().remove(transport);
            if (!transport.getText().isEmpty()) {
                titleLine.getChildren().add(transportSeparator);
                titleLine.getChildren().add(transport);
            }
        });
        getChildren().add(titleLine);
        getChildren().add(new HBox(activityName, spacer, duration));
        getChildren().add(activityWhat);
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
