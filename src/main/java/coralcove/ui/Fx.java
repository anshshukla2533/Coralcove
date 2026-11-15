package coralcove.ui;

import coralcove.util.AppException;
import coralcove.util.DateUtils;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Window;
import javafx.util.StringConverter;
import java.time.LocalDate;

public final class Fx {
    private Fx() {
    }

    public static <T> void run(Supplier<T> work, Consumer<T> onSuccess) {
        run(work, onSuccess, null);
    }

    public static <T> void run(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return work.get();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> {
            Throwable cause = task.getException();
            if (onError != null) {
                onError.accept(cause);
            } else {
                error(cause);
            }
        });
        Thread thread = new Thread(task, "coralcove-db");
        thread.setDaemon(true);
        thread.start();
    }

    public static void error(Throwable cause) {
        String message = cause instanceof AppException && cause.getMessage() != null
                ? cause.getMessage()
                : "Something went wrong. Check data/coralcove.log.";
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> alert(Alert.AlertType.ERROR, "CoralCove", message));
            return;
        }
        alert(Alert.AlertType.ERROR, "CoralCove", message);
    }

    public static void info(String message) {
        alert(Alert.AlertType.INFORMATION, "CoralCove", message);
    }

    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.setTitle("CoralCove");
        dress(alert.getDialogPane());
        Window owner = Window.getWindows().isEmpty() ? null : Window.getWindows().get(0);
        if (owner != null) {
            alert.initOwner(owner);
        }
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static void fadeIn(Node node) {
        if (node == null) {
            return;
        }
        node.setOpacity(0);
        node.setTranslateY(8);
        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), node);
        slide.setFromY(8);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, slide).play();
    }

    public static Label chip(String text, String kind) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().addAll("chip", "chip-" + (kind == null ? "muted" : kind.toLowerCase().replace('_', '-')));
        return label;
    }

    public static FlowPane bar(Node... nodes) {
        FlowPane pane = new FlowPane(8, 8);
        pane.getStyleClass().add("toolbar");
        pane.getChildren().addAll(nodes);
        return pane;
    }

    public static void dress(DialogPane pane) {
        for (Window window : Window.getWindows()) {
            if (window.getScene() != null && !window.getScene().getStylesheets().isEmpty()) {
                pane.getStylesheets().setAll(window.getScene().getStylesheets());
                break;
            }
        }
        pane.getStyleClass().add("desk-dialog");
    }

    public static void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        dress(alert.getDialogPane());
        Window owner = Window.getWindows().isEmpty() ? null : Window.getWindows().get(0);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }

    public static void dates(DatePicker picker) {
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : DateUtils.display(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }
                return DateUtils.parse(text);
            }
        });
        picker.setPromptText("dd-MM-yyyy");
    }

    public static String text(javafx.scene.control.TextInputControl field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
