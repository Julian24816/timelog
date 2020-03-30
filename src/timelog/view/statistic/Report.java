package timelog.view.statistic;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import timelog.statistic.Statistic;
import timelog.statistic.StatisticalDatum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Report extends Alert {
    public Report(Statistic<?, ?>... statistics) {
        super(AlertType.INFORMATION);

        final VBox vBox = new VBox(20);
        for (Statistic<?, ?> statistic : statistics) vBox.getChildren().add(new ReportLine<>(statistic, 1));

        final ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(500);
        getDialogPane().setContent(scrollPane);
        getDialogPane().setPrefSize(250, 400);
        setResizable(true);
    }

    private static class ReportLine<T, D> extends StackPane {
        private final HBox single;
        private final VBox all;
        private boolean expanded;

        public ReportLine(Statistic<T, D> statistic, int expandedDepth) {
            single = getLine(statistic.getName(), statistic.getAggregateData());
            all = getExpandedView(statistic, expandedDepth);

            getChildren().add(single);
            setOnMouseClicked(this::onclick);
            show(expanded = expandedDepth > 0);
        }

        private HBox getLine(String label, StatisticalDatum<?> datum) {
            final Region spacer = new Region();
            spacer.maxWidth(Double.MAX_VALUE);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return new HBox(10, new Text(label), spacer, new Text(datum.toString()));
        }

        private VBox getExpandedView(Statistic<T, D> statistic, int expandedDepth) {
            VBox all = new VBox(5);
            all.setPadding(new Insets(0, 0, 10, 0));
            all.getChildren().add(getLine(statistic.getName(), statistic.getData()));

            final List<Statistic<T, D>> subStatistics = new ArrayList<>(statistic.getSubStatistics());
            Collections.sort(subStatistics);
            for (Statistic<T, D> subStatistic : subStatistics) {
                final ReportLine<T, D> line = new ReportLine<>(subStatistic, Math.max(0, expandedDepth - 1));
                all.getChildren().add(line);
                VBox.setMargin(line, new Insets(0, 0, 0, 10));
            }
            return all.getChildren().size() > 1 ? all : null;
        }

        private void onclick(MouseEvent mouseEvent) {
            if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
            show(expanded = !expanded);
            mouseEvent.consume();
        }

        private void show(boolean expanded) {
            if (all == null) return;
            getChildren().removeAll(single, all);
            getChildren().add(expanded ? all : single);
        }
    }
}
