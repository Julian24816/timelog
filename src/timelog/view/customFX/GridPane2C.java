package timelog.view.customFX;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class GridPane2C extends GridPane {
    private int row;

    public GridPane2C(double space) {
        super();
        setHgap(space);
        setVgap(space);
    }

    public <T extends Node> T addRow(String label, T node) {
        addRow(row++, new Label(label), node);
        GridPane.setHgrow(node, Priority.ALWAYS);
        return node;
    }
}
