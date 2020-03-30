package timelog.view.edit;

import javafx.beans.Observable;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import timelog.model.db.ModelObject;
import timelog.view.customFX.GridPane2C;

abstract class ObjectDialog<T extends ModelObject<T>> extends Dialog<T> {

    protected final T editedObject;
    protected final GridPane2C gridPane2C;

    private final Button okButton;
    private BooleanExpression okButtonDisabled;

    ObjectDialog(String name, T editedObject) {
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
        okButtonDisabled = BooleanExpression.booleanExpression(new SimpleBooleanProperty(false));
        setResultConverter(this::convertResult);
    }

    void addOKRequirement(ObservableBooleanValue value) {
        okButtonDisabled.removeListener(this::onDisableInvalidated);
        okButtonDisabled = okButtonDisabled.or(BooleanExpression.booleanExpression(value).not());
        okButtonDisabled.addListener(this::onDisableInvalidated);
    }

    private void onDisableInvalidated(Observable observable) {
        okButton.setDisable(okButtonDisabled.getValue());
    }

    private T convertResult(ButtonType buttonType) {
        if (!buttonType.equals(ButtonType.OK)) return null;
        else if (editedObject == null) return createNew();
        else return save() ? editedObject : null;
    }

    protected abstract T createNew();

    protected abstract boolean save();
}
