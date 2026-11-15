package coralcove.ui;

import coralcove.Main;
import coralcove.model.User;
import coralcove.service.AuthenticationService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;

public final class ChangePasswordDialog {
    private ChangePasswordDialog() {
    }

    public static boolean show(User user, boolean forced) {
        Dialog<ButtonType> dialog = new Dialog<>();
        if (Main.stage() != null) {
            dialog.initOwner(Main.stage());
        }
        dialog.setTitle(forced ? "Change password" : "Password");
        dialog.setHeaderText(forced
                ? "Choose a new password before continuing."
                : "Change the password for " + user.getUsername() + ".");
        PasswordField current = new PasswordField();
        PasswordField next = new PasswordField();
        PasswordField confirm = new PasswordField();
        Label error = new Label();
        error.getStyleClass().add("error");
        error.setWrapText(true);
        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(12);
        form.setPadding(new Insets(18, 24, 14, 24));
        form.add(new Label("Current"), 0, 0);
        form.add(current, 1, 0);
        form.add(new Label("New"), 0, 1);
        form.add(next, 1, 1);
        form.add(new Label("Confirm"), 0, 2);
        form.add(confirm, 1, 2);
        form.add(error, 0, 3, 2, 1);
        dialog.getDialogPane().setContent(form);
        Fx.dress(dialog.getDialogPane());
        dialog.getDialogPane().setPrefWidth(480);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Save");
        AuthenticationService auth = new AuthenticationService();
        ok.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            if (!next.getText().equals(confirm.getText())) {
                error.setText("The new password and the confirmation do not match.");
                return;
            }
            String now = current.getText() == null ? "" : current.getText();
            String chosen = next.getText() == null ? "" : next.getText();
            ok.setDisable(true);
            Fx.run(() -> {
                auth.changePassword(user.getId(), now, chosen);
                return Boolean.TRUE;
            }, done -> {
                dialog.setResult(ButtonType.OK);
                dialog.close();
            }, cause -> {
                ok.setDisable(false);
                error.setText(cause.getMessage() == null ? "Could not change the password." : cause.getMessage());
            });
        });
        return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
