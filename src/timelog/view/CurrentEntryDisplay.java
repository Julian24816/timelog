package timelog.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import timelog.model.LogEntry;
import timelog.model.MeansOfTransport;
import timelog.view.customFX.CustomBindings;
import timelog.view.customFX.JoiningTextFlow;
import timelog.view.customFX.TimeTextField;
import timelog.view.edit.LogEntryDialog;
import timelog.view.single.EndTimeDialog;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class CurrentEntryDisplay extends GridPane {

    private final Timer timer = new Timer(true);
    private final Text startTime = new Text("--:--");
    private final Text duration = new Text("--:--");
    private final Button button = new Button("New");
    private final Text activityName = new Text("No Current Activity");
    private final StringProperty what = new SimpleStringProperty(), transport = new SimpleStringProperty();

    private final ObjectProperty<LogEntry> entry = new SimpleObjectProperty<>(this, "current activity") {
        @Override
        protected void invalidated() {
            if (getValue() == null) {
                startTime.setText("--:--");
                activityName.setText("No Current Activity");
                duration.setText("--:--");
                button.setText("New");
                transport.unbind();
                what.unbind();
            } else {
                startTime.setText(TimeTextField.TIME_FORMATTER.format(getValue().getStart().toLocalTime()));
                activityName.setText(getValue().getActivity().getName());
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (getValue() == null) cancel();
                        else {
                            Duration duration;
                            if (getValue().getEnd() == null)
                                duration = Duration.between(getValue().getStart(), LocalDateTime.now());
                            else duration = Duration.between(getValue().getEnd(), getValue().getStart());
                            CurrentEntryDisplay.this.duration.setText(TimeTextField.TIME_FORMATTER.format(duration.addTo(LocalTime.MIN)));
                        }
                    }
                }, 0, 500);
                button.setText("Stop");
                transport.bind(CustomBindings.select(getValue().meansOfTransportProperty(), MeansOfTransport::nameProperty));
                what.bind(getValue().whatProperty());
            }
        }
    };

    private final Consumer<LogEntry> newCompleteEntry;

    public CurrentEntryDisplay(Consumer<LogEntry> newCompleteEntry) {
        super();
        this.newCompleteEntry = newCompleteEntry;

        entry.setValue(LogEntry.FACTORY.getUnfinishedEntry());
        button.setOnAction(this::onButtonPress);
        setOnMouseClicked(this::doubleClick);

        createLayout();
    }

    private void onButtonPress(ActionEvent event) {
        if (entry.get() == null) {
            final Optional<LogEntry> logEntry = new LogEntryDialog().showAndWait();
            logEntry.ifPresent(value -> {
                if (value.getEnd() == null) entry.setValue(value);
                else newCompleteEntry.accept(value);
            });
        } else {
            final Optional<LocalDateTime> endTime = new EndTimeDialog().showAndWait();
            endTime.ifPresent(end -> {
                entry.get().endProperty().setValue(end);
                if (LogEntry.FACTORY.update(entry.get())) {
                    newCompleteEntry.accept(entry.get());
                    entry.set(LogEntry.FACTORY.getUnfinishedEntry());
                }
            });
        }
    }

    private void doubleClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() != 2 || !mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
        new LogEntryDialog(entry.get()).showAndWait().ifPresent(logEntry -> {
            if (logEntry.getEnd() == null) entry.setValue(logEntry);
            else {
                entry.setValue(null);
                newCompleteEntry.accept(logEntry);
            }
        });
    }

    private void createLayout() {
        activityName.setFont(new Font(16));

        Label title = new Label("Current Activity, ");
        title.setLabelFor(activityName);

        Label startTimeLabel = new Label("started at ");
        startTimeLabel.setLabelFor(startTime);

        Region spacer = new Region();
        spacer.setPrefWidth(30);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        GridPane.setHalignment(duration, HPos.CENTER);
        addRow(0, new HBox(title, startTimeLabel, startTime), duration);
        addRow(1, new JoiningTextFlow(activityName, what, transport), button);

        final ColumnConstraints grow = new ColumnConstraints();
        grow.setHgrow(Priority.ALWAYS);
        getColumnConstraints().add(grow);
    }
}
