package timelog.view.customFX;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorAlert extends Alert {
    public ErrorAlert(Throwable exception) {
        super(AlertType.ERROR, String.format("Exception: %s", exception.getMessage()));

        final StringWriter errorMessage = new StringWriter();
        exception.printStackTrace(new PrintWriter(errorMessage));
        final TextArea textArea = new TextArea(errorMessage.toString());
        textArea.setEditable(false);
        getDialogPane().setExpandableContent(textArea);
    }

    public static void show(Throwable e) {
        new ErrorAlert(e).show();
    }
}
