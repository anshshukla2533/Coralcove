package coralcove.ui;

import coralcove.Main;
import coralcove.model.User;
import coralcove.service.AuthenticationService;
import coralcove.service.Session;
import coralcove.util.AppException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class LoginView {
    private static LoginView live;
    private Timeline drift;
    private final AuthenticationService auth = new AuthenticationService();
    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final PasswordField current = new PasswordField();
    private final PasswordField next = new PasswordField();
    private final PasswordField confirm = new PasswordField();
    private final Label signError = new Label();
    private final Label changeError = new Label();
    private final Button signIn = new Button("Sign in");
    private final Button save = new Button("Save password");
    private final VBox signForm = new VBox(12);
    private final VBox changeForm = new VBox(12);
    private final StackPane forms = new StackPane();
    private User pending;
    private boolean busy;

    public LoginView() {
        live = this;
        username.setPromptText("Username");
        password.setPromptText("Password");
        current.setPromptText("Current password");
        next.setPromptText("New password");
        confirm.setPromptText("Confirm new password");
        signError.getStyleClass().add("error");
        changeError.getStyleClass().add("error");
        signError.setWrapText(true);
        changeError.setWrapText(true);
        signIn.getStyleClass().add("primary");
        save.getStyleClass().add("primary");
        signIn.setMaxWidth(Double.MAX_VALUE);
        save.setMaxWidth(Double.MAX_VALUE);
        signIn.setOnAction(event -> submit());
        password.setOnAction(event -> submit());
        username.setOnAction(event -> password.requestFocus());
        save.setOnAction(event -> savePassword());
        confirm.setOnAction(event -> savePassword());

        Label hint = note("First desk login is admin. Temporary password is Admin@123.");
        hint.getStyleClass().add("hint");
        signForm.getStyleClass().add("login-fields");
        signForm.getChildren().addAll(
                heading("Sign in"),
                note("Use the account issued for this desk."),
                username, password, signError, signIn, hint);

        Label rules = note("Use at least 8 characters, with a letter and a digit. It must differ from the current password.");
        rules.getStyleClass().add("hint");
        Button back = new Button("Back");
        back.getStyleClass().add("secondary");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(event -> showSignIn());
        changeForm.getStyleClass().add("login-fields");
        changeForm.getChildren().addAll(
                heading("Choose a new password"),
                note("Replace the temporary password before the desk opens."),
                current, next, confirm, rules, changeError, save, back);
        forms.getChildren().add(signForm);
        forms.getStyleClass().add("login-form");
    }

    public Parent root() {
        VBox brand = new VBox(14);
        brand.getStyleClass().add("login-brand");
        Label kicker = new Label("Resort desk");
        kicker.getStyleClass().add("login-kicker");
        Label mark = new Label("CoralCove");
        mark.getStyleClass().add("wordmark");
        Region rule = new Region();
        rule.getStyleClass().add("gold-rule");
        Label blurb = new Label("Stays, lodges, arrivals, and billing for the resort group.");
        blurb.getStyleClass().add("login-blurb");
        blurb.setWrapText(true);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label foot = new Label("Beach  ·  Lagoon  ·  Garden  ·  Cliff");
        foot.getStyleClass().add("login-foot");
        brand.getChildren().addAll(kicker, mark, rule, blurb, spacer, foot);

        BorderPane page = new BorderPane();
        page.getStyleClass().add("login-page");
        page.setLeft(brand);
        page.setCenter(forms);
        StackPane.setAlignment(signForm, Pos.CENTER_LEFT);

        StackPane stage = new StackPane();
        ImageView waves = new ImageView(ResortArt.image("sunset-waves"));
        waves.getStyleClass().add("login-waves");
        waves.setPreserveRatio(false);
        waves.fitWidthProperty().bind(stage.widthProperty().multiply(1.16));
        waves.fitHeightProperty().bind(stage.heightProperty().multiply(1.16));
        Region veil = new Region();
        veil.getStyleClass().add("login-veil");
        stage.getChildren().addAll(waves, veil, page);
        drift = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(waves.translateXProperty(), 0),
                        new KeyValue(waves.translateYProperty(), 0)),
                new KeyFrame(Duration.seconds(16),
                        new KeyValue(waves.translateXProperty(), -48),
                        new KeyValue(waves.translateYProperty(), -22)));
        drift.setAutoReverse(true);
        drift.setCycleCount(Timeline.INDEFINITE);
        drift.play();
        return stage;
    }

    public static void stopMotion() {
        if (live != null && live.drift != null) {
            live.drift.stop();
        }
    }

    private void submit() {
        if (busy) {
            return;
        }
        signError.setText("");
        String name = Fx.text(username);
        String secret = password.getText() == null ? "" : password.getText();
        setBusy(true);
        Fx.run(() -> auth.login(name, secret), user -> {
            setBusy(false);
            signedIn(user, secret);
        }, cause -> {
            setBusy(false);
            signError.setText(message(cause, "Could not sign in."));
        });
    }

    private void signedIn(User user, String secret) {
        Session.set(user);
        if (!user.isMustChangePassword()) {
            Main.showShell();
            return;
        }
        pending = user;
        current.setText(secret);
        next.clear();
        confirm.clear();
        changeError.setText("");
        forms.getChildren().setAll(changeForm);
        Fx.fadeIn(changeForm);
        next.requestFocus();
    }

    private void savePassword() {
        if (busy || pending == null) {
            return;
        }
        changeError.setText("");
        String now = current.getText() == null ? "" : current.getText();
        String chosen = next.getText() == null ? "" : next.getText();
        String again = confirm.getText() == null ? "" : confirm.getText();
        if (!chosen.equals(again)) {
            changeError.setText("The new password and the confirmation do not match.");
            return;
        }
        long userId = pending.getId();
        setBusy(true);
        Fx.run(() -> {
            auth.changePassword(userId, now, chosen);
            return Boolean.TRUE;
        }, done -> {
            setBusy(false);
            Main.showShell();
        }, cause -> {
            setBusy(false);
            changeError.setText(message(cause, "Could not change the password."));
        });
    }

    private void showSignIn() {
        if (busy) {
            return;
        }
        pending = null;
        Session.clear();
        changeError.setText("");
        forms.getChildren().setAll(signForm);
        Fx.fadeIn(signForm);
        password.requestFocus();
    }

    private void setBusy(boolean value) {
        busy = value;
        signIn.setDisable(value);
        save.setDisable(value);
        signIn.setText(value ? "Signing in…" : "Sign in");
        save.setText(value ? "Saving…" : "Save password");
    }

    private static Label heading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("login-heading");
        label.setWrapText(true);
        label.setMaxWidth(400);
        return label;
    }

    private static Label note(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(400);
        return label;
    }

    private static String message(Throwable cause, String fallback) {
        if (cause instanceof AppException && cause.getMessage() != null) {
            return cause.getMessage();
        }
        return fallback;
    }
}
