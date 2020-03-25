package timelog.view.customFX;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import timelog.model.db.DatabaseObject;

abstract class ObjectDialog<T extends DatabaseObject<T>> extends Dialog<T> {

    protected final T editedObject;

    protected final GridPane2C gridPane2C;
    protected final Button okButton;

    ObjectDialog(String name, T editedObject, boolean okDisabled) {
        super();
        this.editedObject = editedObject;
        setTitle(name);
        setHeaderText((editedObject == null ? "New" : "Edit") + " Activity");

        gridPane2C = new GridPane2C(10);
        final TextField id = gridPane2C.addRow("id", new TextField());
        id.setText(editedObject == null ? "<new>" : String.valueOf(editedObject.getId()));
        id.setDisable(true);
        getDialogPane().setContent(gridPane2C);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(editedObject == null && okDisabled);
        setResultConverter(this::convertResult);
    }

    private T convertResult(ButtonType buttonType) {
        if (!buttonType.equals(ButtonType.OK)) return null;
        else if (editedObject == null) return createNew();
        else return save() ? editedObject : null;
    }

    protected abstract T createNew();

    protected abstract boolean save();
}
