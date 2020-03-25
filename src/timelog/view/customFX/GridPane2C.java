package timelog.view.customFX;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

public class GridPane2C extends GridPane {
    private int row;

    public GridPane2C(double space) {
        super();
        setHgap(space);
        setVgap(space);
    }

    public <T extends Node> T addRow(String label, T node) {
        final Label labelNode = new Label(label);
        labelNode.setLabelFor(node);
        addRow(row++, labelNode, node);
        GridPane.setHgrow(node, Priority.ALWAYS);
        return node;
    }

    public void addSeparator() {
        addRow(row++, new Text(""), new Separator());
    }
}
