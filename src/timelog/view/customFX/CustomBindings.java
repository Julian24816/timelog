package timelog.view.customFX;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

public final class CustomBindings {
    private CustomBindings() {
    }

    public static <P, R> ObjectBinding<R> ifNull(ObservableValue<P> observable,
                                                 Function<P, R> select, R ifNull) {
        return new ObjectBinding<>() {
            {
                bind(observable);
            }

            @Override
            protected R computeValue() {
                return observable.getValue() == null ? ifNull : select.apply(observable.getValue());
            }
        };
    }

    public static <P, R> ObjectExpression<R> apply(ObservableValue<P> observableValue, Function<P, R> select) {
        return new ObjectBinding<>() {
            {
                bind(observableValue);
            }

            @Override
            protected R computeValue() {
                return select.apply(observableValue.getValue());
            }
        };
    }

    public static <P, I, R> ObjectExpression<R> select(ObservableValue<P> observableValue,
                                                       Function<P, ObservableValue<I>> intermediate,
                                                       Function<I, ObservableValue<R>> select) {
        return select(select(observableValue, intermediate), select);
    }

    public static <P, R> ObjectExpression<R> select(ObservableValue<P> observableValue,
                                                    Function<P, ObservableValue<R>> select) {
        ObjectProperty<R> property = new SimpleObjectProperty<>();
        Runnable bindToCurrentValue = () ->
                Optional.ofNullable(observableValue.getValue()).map(select).ifPresentOrElse(property::bind, property::unbind);
        bindToCurrentValue.run();
        observableValue.addListener(observable -> bindToCurrentValue.run());
        return property;
    }

    public static BooleanExpression matches(TextField textField, String regex) {
        return new BooleanBinding() {
            {
                bind(textField.textProperty());
            }

            @Override
            protected boolean computeValue() {
                return textField.getText().matches(regex);
            }
        };
    }

    public static BooleanExpression isBefore(ObservableValue<LocalDate> localDateObservableValue, LocalDate localDateTarget) {
        return new BooleanBinding() {
            {
                bind(localDateObservableValue);
            }

            @Override
            protected boolean computeValue() {
                return localDateObservableValue.getValue() != null && localDateObservableValue.getValue().isBefore(localDateTarget);
            }
        };
    }
}
