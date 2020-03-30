package timelog.view.edit;

import javafx.scene.control.TextField;
import timelog.model.MeansOfTransport;

class MeansOfTransportDialog extends ObjectDialog<MeansOfTransport> {
    private final TextField name;

    public MeansOfTransportDialog() {
        this(null);
    }

    public MeansOfTransportDialog(MeansOfTransport editedObject) {
        super("Activity", editedObject);

        name = gridPane2C.addRow("Name", new TextField());
        name.setPromptText("enter name");
        addOKRequirement(name.textProperty().isNotEmpty());

        name.requestFocus();

        if (editedObject != null) {
            name.setText(editedObject.getName());
        }
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
