package coralcove.ui;

import coralcove.Main;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

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
        form.setHgap(12);
        form.setVgap(10);
        dialog.getDialogPane().setContent(form);
        Fx.dress(dialog.getDialogPane());
        dialog.getDialogPane().setPrefWidth(460);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Save");
        ok.getStyleClass().add("primary");
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
