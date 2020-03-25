package timelog.view.edit;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import timelog.model.Activity;
import timelog.model.MeansOfTransport;

class MeansOfTransportDialog extends ObjectDialog<MeansOfTransport> {
    private final TextField name;

    public MeansOfTransportDialog() {
        this(null);
    }

    public MeansOfTransportDialog(MeansOfTransport editedObject) {
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
    protected MeansOfTransport createNew() {
        return MeansOfTransport.FACTORY.createNew(name.getText());
    }

    @Override
    protected boolean save() {
        editedObject.setName(name.getText());
        return MeansOfTransport.FACTORY.update(editedObject);
    }
}
