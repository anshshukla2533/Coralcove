package coralcove.ui;

import coralcove.Main;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public final class Dialogs {
    private Dialogs() {
    }

    public static void form(String title, GridPane form, Runnable save) {
        Dialog<ButtonType> dialog = new Dialog<>();
        if (Main.stage() != null) {
            dialog.initOwner(Main.stage());
        }
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        form.setPadding(new Insets(20, 26, 16, 26));
        form.setHgap(18);
        form.setVgap(12);

        if (form.getColumnConstraints().isEmpty()) {
            ColumnConstraints col0 = new ColumnConstraints();
            col0.setMinWidth(110);
            col0.setPrefWidth(130);
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHgrow(Priority.ALWAYS);
            form.getColumnConstraints().addAll(col0, col1);
        }

        form.getChildren().forEach(child -> {
            Integer col = GridPane.getColumnIndex(child);
            if (col != null && col == 1 && child instanceof Control control) {
                control.setMaxWidth(Double.MAX_VALUE);
            }
        });

        ScrollPane scroller = new ScrollPane(form);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.getStyleClass().add("desk-scroll");
        scroller.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");

        dialog.getDialogPane().setContent(scroller);
        Fx.dress(dialog.getDialogPane());
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().setMaxHeight(680);
        dialog.setResizable(true);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Save");
        ok.getStyleClass().add("primary");
        ok.setMinWidth(100);

        Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancel != null) {
            cancel.setMinWidth(80);
        }

        ok.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                save.run();
            } catch (RuntimeException ex) {
                event.consume();
                Fx.error(ex);
            }
        });
        dialog.showAndWait();
    }
}
