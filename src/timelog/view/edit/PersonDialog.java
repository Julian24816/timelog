package timelog.view.edit;

import javafx.beans.Observable;
import javafx.scene.control.TextField;
import timelog.model.MeansOfTransport;
import timelog.model.Person;

class PersonDialog extends ObjectDialog<Person> {
    private final TextField name;

    public PersonDialog() {
        this(null);
    }

    public PersonDialog(Person editedObject) {
        super("Activity", editedObject, true);

        name = gridPane2C.addRow("Name", new TextField());
        name.setPromptText("enter name");
        name.textProperty().addListener(this::invalidated);
        name.requestFocus();

        if (editedObject != null) {
            name.setText(editedObject.getName());
        }
    }

    private void invalidated(Observable observable) {
        okButton.setDisable(name.getText().isEmpty());
    }

    @Override
    protected Person createNew() {
        return Person.FACTORY.createNew(name.getText());
    }

    @Override
    protected boolean save() {
        editedObject.setName(name.getText());
        return Person.FACTORY.update(editedObject);
    }
}
