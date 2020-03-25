package timelog.view.customFX;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import timelog.model.db.DatabaseObject;
import timelog.model.db.ModelObject;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class CreatingChoiceBox<T extends ModelObject<T>> extends HBox {

    private final ReadOnlyObjectWrapper<T> valueProperty = new ReadOnlyObjectWrapper<>();
    private final ObservableList<T> items = FXCollections.observableArrayList();

    private final ChoiceBox<Entry<T>> choiceBox;
    private final Supplier<Dialog<T>> newDialog;
    private final Function<T, Dialog<T>> editDialog;

    public CreatingChoiceBox(Collection<T> items) {
        this(items, null, null, false);
    }

    public CreatingChoiceBox(Collection<T> items, Supplier<Dialog<T>> newDialog, Function<T, Dialog<T>> editDialog, boolean allowSelectNull) {
        super(10);
        this.newDialog = newDialog;
        this.editDialog = editDialog;

        choiceBox = new ChoiceBox<>();
        getChildren().add(choiceBox);
        HBox.setHgrow(choiceBox, Priority.ALWAYS);
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        choiceBox.setConverter(Entry.getStringConverter());
        choiceBox.getSelectionModel().selectedItemProperty().addListener(this::selectionChanged);
        if (newDialog != null) choiceBox.getItems().add(Entry.placeholder());
        if (editDialog != null) choiceBox.setOnMouseClicked(this::doubleClick);

        this.items.addListener(this::onListChanged);
        this.items.addAll(items);

        if (editDialog != null) addButton("Edit", this::showEditDialog);
        if (allowSelectNull) addButton("Remove", () -> choiceBox.getSelectionModel().select(null));
    }

    private void selectionChanged(ObservableValue<?> observableValue, Entry<T> oldValue, Entry<T> newValue) {
        if (newValue == null) valueProperty.set(null);
        else if (newValue.isPlaceholder()) {
            assert newDialog != null;
            // show a new dialog when the "new..." placeholder is selected, then select result or previous value
            final Optional<T> optionalItem = newDialog.get().showAndWait();
            if (optionalItem.isPresent()) {
                final Entry<T> entry = Entry.of(optionalItem.get());
                choiceBox.getItems().add(entry);
                FXCollections.sort(choiceBox.getItems());
                choiceBox.setValue(entry);
            } else choiceBox.getSelectionModel().select(oldValue);
        } else {
            valueProperty.set(newValue.get());
        }
    }

    private void doubleClick(MouseEvent event) {
        if (!event.getButton().equals(MouseButton.PRIMARY) || event.getClickCount() != 2 || choiceBox.getValue() == null)
            return;
        showEditDialog();
    }

    private void onListChanged(ListChangeListener.Change<? extends T> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be permutated"));
            } else if (c.wasUpdated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be updated"));
            } else {
                for (T added : c.getAddedSubList()) choiceBox.getItems().add(Entry.of(added));
                for (T removed : c.getRemoved()) choiceBox.getItems().remove(Entry.of(removed));
                FXCollections.sort(choiceBox.getItems());
            }
        }
    }

    private void addButton(String text, Runnable onAction) {
        Button selectNullButton = new Button(text);
        selectNullButton.setOnAction(event -> onAction.run());
        bindSelectedToEnabled(selectNullButton);
        getChildren().add(selectNullButton);
    }

    private void showEditDialog() {
        editDialog.apply(choiceBox.getValue().content).show();
    }

    private void bindSelectedToEnabled(Button button) {
        button.setDisable(true);
        choiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> button.setDisable(newValue == null));
    }

    public CreatingChoiceBox(Collection<T> items, boolean allowSelectNull) {
        this(items, null, null, allowSelectNull);
    }

    public CreatingChoiceBox(Collection<T> items, Supplier<Dialog<T>> newDialog) {
        this(items, newDialog, null, false);
    }

    public CreatingChoiceBox(Collection<T> items, Supplier<Dialog<T>> newDialog, boolean allowSelectNull) {
        this(items, newDialog, null, allowSelectNull);
    }

    public CreatingChoiceBox(Collection<T> items, Supplier<Dialog<T>> newDialog, Function<T, Dialog<T>> editDialog) {
        this(items, newDialog, editDialog, false);
    }

    public ObservableList<T> getItems() {
        return items;
    }

    public ReadOnlyObjectProperty<T> valueProperty() {
        return valueProperty.getReadOnlyProperty();
    }

    public T getValue() {
        return valueProperty.get();
    }

    public void setValue(T value) {
        if (value == null) choiceBox.getSelectionModel().select(null);
        else {
            if (!items.contains(value)) items.add(value);
            choiceBox.getSelectionModel().select(Entry.of(value));
        }
    }

    public static final class Entry<T extends ModelObject<T>> implements Comparable<Entry<T>> {
        private final T content;

        private Entry(T content) {
            this.content = content;
        }

        public static <T extends ModelObject<T>> StringConverter<Entry<T>> getStringConverter() {
            return new StringConverter<>() {
                @Override
                public String toString(Entry<T> entry) {
                    return entry.isPlaceholder() ? "new..." : entry.get().getDisplayName();
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

        public static <T extends ModelObject<T>> Entry<T> placeholder() {
            return new Entry<>(null);
        }

        public static <T extends ModelObject<T>> Entry<T> of(T obj) {
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
