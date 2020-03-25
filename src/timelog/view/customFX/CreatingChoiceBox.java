package timelog.view.customFX;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import timelog.model.db.DatabaseObject;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class CreatingChoiceBox<T extends DatabaseObject<T>> extends HBox {

    private final ReadOnlyObjectWrapper<T> valueProperty = new ReadOnlyObjectWrapper<>();
    private final ChoiceBox<Entry<T>> choiceBox = new ChoiceBox<>();
    private final Function<T, Dialog<T>> editDialog;

    public CreatingChoiceBox(Collection<T> items, Supplier<Dialog<T>> newDialog, Function<T, Dialog<T>> editDialog) {
        this(items, newDialog, editDialog, false);
    }

    public CreatingChoiceBox(Collection<T> items, Supplier<Dialog<T>> newDialog, Function<T, Dialog<T>> editDialog, boolean allowSelectNull) {
        super(10);
        this.editDialog = editDialog;
        getChildren().add(choiceBox);
        HBox.setHgrow(choiceBox, Priority.ALWAYS);
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        choiceBox.setConverter(Entry.getStringConverter());
        choiceBox.setOnMouseClicked(this::doubleClick);

        // fill choiceBox with items and a "new..." placeholder
        choiceBox.getItems().add(Entry.placeholder());
        items.forEach(item -> choiceBox.getItems().add(Entry.of(item)));
        FXCollections.sort(choiceBox.getItems());

        // bind value property to choice box selection and ...
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) valueProperty.set(null);
            else if (!newValue.isPlaceholder()) {
                valueProperty.set(newValue.get());
            } else {

                // show a new dialog when the "new..." placeholder is selected, then select result or previous value
                final Optional<T> optionalItem = newDialog.get().showAndWait();
                if (optionalItem.isPresent()) {
                    final Entry<T> entry = Entry.of(optionalItem.get());
                    choiceBox.getItems().add(entry);
                    FXCollections.sort(choiceBox.getItems());
                    choiceBox.setValue(entry);
                } else choiceBox.getSelectionModel().select(oldValue);
            }
        });

        // if selected show a select null button
        if (allowSelectNull) {
            Button selectNullButton = new Button("Entfernen");
            selectNullButton.setOnAction(event -> choiceBox.getSelectionModel().select(null));
            selectNullButton.setDisable(true);
            choiceBox.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> selectNullButton.setDisable(newValue == null));
            getChildren().add(selectNullButton);
        }
    }

    public void doubleClick(MouseEvent event) {
        if (!event.getButton().equals(MouseButton.PRIMARY) || event.getClickCount() != 2 || choiceBox.getValue() == null) return;
        editDialog.apply(choiceBox.getValue().content).show();
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return valueProperty.getReadOnlyProperty();
    }

    public T getValue() {
        return valueProperty.get();
    }

    public void setValue(T value) {
        if (value == null) choiceBox.getSelectionModel().select(null);
        else choiceBox.getSelectionModel().select(Entry.of(value));
        //TODO decide what happens when this entry does not yet exist
    }

    public static final class Entry<T extends DatabaseObject<T>> implements Comparable<Entry<T>> {
        private final T content;

        public Entry(T content) {
            this.content = content;
        }

        public static <T extends DatabaseObject<T>> StringConverter<Entry<T>> getStringConverter() {
            return new StringConverter<>() {
                @Override
                public String toString(Entry<T> entry) {
                    return entry.isPlaceholder() ? "Neu..." : entry.get().getDisplayName();
                }

                @Override
                public Entry<T> fromString(String s) {
                    return null;
                }
            };
        }

        public boolean isPlaceholder() {
            return content == null;
        }

        public T get() {
            return content;
        }

        public static <T extends DatabaseObject<T>> Entry<T> placeholder() {
            return new Entry<>(null);
        }

        public static <T extends DatabaseObject<T>> Entry<T> of(T obj) {
            return new Entry<>(Objects.requireNonNull(obj));
        }

        @Override
        public String toString() {
            return isPlaceholder() ? "PlaceholderEntry" : "Entry{content=" + content + "}";
        }

        @Override
        public int compareTo(Entry<T> o) {
            if (content == null && o.content == null) return 0;
            if (content == null) return 1;
            if (o.content == null) return -1;
            return content.compareTo(o.content);
        }

        @Override
        public boolean equals(Object o) {
            //noinspection ObjectComparison
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;
            Entry<?> entry = (Entry<?>) o;
            return Objects.equals(content, entry.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }
    }
}
