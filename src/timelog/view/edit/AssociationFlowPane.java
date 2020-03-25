package timelog.view.edit;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import timelog.model.db.Association;
import timelog.model.db.AssociationFactory;
import timelog.model.db.ModelObject;
import timelog.view.customFX.CreatingChoiceBox;
import timelog.view.customFX.ErrorAlert;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AssociationFlowPane<A extends ModelObject<A>, B extends ModelObject<B>, T extends Association<A, B>> extends FlowPane {

    private final AssociationFactory<A, B, T> factory;
    private final A first;
    private final Supplier<Dialog<B>> newDialog;
    private final Function<B, Dialog<B>> editDialog;

    private final CreatingChoiceBox<B> bChoiceBox;

    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final ObservableList<PendingAssociation<A, B>> pendingItems = FXCollections.observableArrayList();

    public AssociationFlowPane(AssociationFactory<A, B, T> factory, A first, Collection<B> choices, Supplier<Dialog<B>> newDialog, Function<B, Dialog<B>> editDialog) {
        super(Orientation.HORIZONTAL, 10, 10);
        this.factory = factory;
        this.first = first;
        this.newDialog = newDialog;
        this.editDialog = editDialog;

        bChoiceBox = new CreatingChoiceBox<>(choices, newDialog);
        getChildren().add(bChoiceBox);
        bChoiceBox.valueProperty().addListener(this::onChoiceBoxChanged);

        items.addListener(this::onListChanged);
        pendingItems.addListener(this::onPendingListChanged);
        if (first != null) items.addAll(factory.getAll(first));
    }

    public Collection<T> associateAll(A first) {
        if (this.first != null) throw new IllegalStateException();
        List<T> list = new LinkedList<>();
        pendingItems.forEach(pending -> Optional.ofNullable(factory.create(first, pending.with)).ifPresent(list::add));
        pendingItems.clear();
        return list;
    }

    private void onChoiceBoxChanged(Observable observable) {
        if (bChoiceBox.getValue() == null) return;
        final B second = bChoiceBox.getValue();
        bChoiceBox.setValue(null);
        if (first == null) pendingItems.add(new PendingAssociation<>(second));
        else {
            final T association = factory.create(first, second);
            if (association != null) items.add(association);
        }
    }

    private void onListChanged(ListChangeListener.Change<? extends T> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be permutated"));
            } else if (c.wasUpdated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be updated"));
            } else {
                for (T added : c.getAddedSubList()) {
                    bChoiceBox.getItems().remove(added.getSecond());
                    getChildren().add(getChildren().size() - 1, new AssociationItem<>(
                            added.getSecond(), editDialog, b -> {
                        if (factory.delete(added)) items.remove(added);

                    }));
                }
                for (T removed : c.getRemoved()) {
                    bChoiceBox.getItems().add(removed.getSecond());
                    getChildren().remove(new AssociationItem<>(removed.getSecond(), null, null));
                }
            }
        }
    }

    private void onPendingListChanged(ListChangeListener.Change<? extends PendingAssociation<A, B>> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be permutated"));
            } else if (c.wasUpdated()) {
                ErrorAlert.show(new UnsupportedOperationException("entries list must not be updated"));
            } else {
                for (PendingAssociation<A, B> added : c.getAddedSubList()) {
                    bChoiceBox.getItems().remove(added.with);
                    getChildren().add(getChildren().size() - 1, new AssociationItem<>(
                            added.with, editDialog, b -> pendingItems.remove(added)));
                }
                for (PendingAssociation<A, B> removed : c.getRemoved()) {
                    bChoiceBox.getItems().add(removed.with);
                    getChildren().remove(new AssociationItem<>(removed.with, null, null));
                }
            }
        }
    }

    private static final class PendingAssociation<A extends ModelObject<A>, B extends ModelObject<B>> {
        private final B with;

        private PendingAssociation(B with) {
            this.with = Objects.requireNonNull(with);
        }

        @Override
        public boolean equals(Object o) {
            //noinspection ObjectComparison
            if (this == o) return true;
            if (o == null || !getClass().equals(o.getClass())) return false;
            PendingAssociation<?, ?> that = (PendingAssociation<?, ?>) o;
            return with.equals(that.with);
        }

        @Override
        public int hashCode() {
            return Objects.hash(with);
        }
    }

    private static final class AssociationItem<T extends ModelObject<T>> extends HBox {
        private final T item;

        private AssociationItem(T item, Function<T, Dialog<T>> editDialog, Consumer<T> onRemove) {
            super();
            this.item = item;
            Button editButton = new Button(item.getDisplayName());
            editButton.setOnAction(event -> editDialog.apply(item).show());
            Button removeButton = new Button("X");
            removeButton.setOnAction(event -> onRemove.accept(item));
            getChildren().addAll(editButton, removeButton);
            //TODO sorting?
        }

        @Override
        public boolean equals(Object o) {
            //noinspection ObjectComparison
            if (this == o) return true;
            if (o == null || !getClass().equals(o.getClass())) return false;
            AssociationItem<?> that = (AssociationItem<?>) o;
            return item.equals(that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item);
        }
    }
}
