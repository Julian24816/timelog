package timelog.view.edit;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import timelog.model.Activity;
import timelog.view.customFX.CreatingChoiceBox;

class ActivityDialog extends ObjectDialog<Activity> {
    private final CreatingChoiceBox<Activity> parent;
    private final TextField name;

    public ActivityDialog() {
        this(null);
    }

    public ActivityDialog(Activity editedObject) {
        super("Activity", editedObject, true);

        parent = gridPane2C.addRow("Parent", new CreatingChoiceBox<>(Activity.FACTORY.getAll()));
        parent.setValue(Activity.FACTORY.getForId(0));
        parent.valueProperty().addListener(this::invalidated);

        name = gridPane2C.addRow("Name", new TextField());
        name.setPromptText("enter name");
        name.textProperty().addListener(this::invalidated);
        name.requestFocus();

        if (editedObject != null) {
            parent.setValue(editedObject.getParent());
            name.setText(editedObject.getName());
        }
    }

    private void invalidated(Observable observable) {
        okButton.setDisable(parent.getValue() == null || name.getText().isEmpty());
    }

    @Override
    protected Activity createNew() {
        return Activity.FACTORY.createNew(
                parent.getValue(),
                name.getText()
        );
    }

    @Override
    protected boolean save() {
        editedObject.setParent(parent.getValue());
        editedObject.setName(name.getText());
        return Activity.FACTORY.update(editedObject);
    }
}
