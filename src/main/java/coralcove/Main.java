package coralcove;

import coralcove.config.AppConfig;
import coralcove.database.DatabaseManager;
import coralcove.service.Session;
import coralcove.ui.Fx;
import coralcove.ui.LoginView;
import coralcove.ui.ShellView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage stage;
    private static ShellView shell;

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage stage() {
        return stage;
    }

    public static void showLogin() {
        shell = null;
        Session.clear();
        stage.getScene().setRoot(new LoginView().root());
    }

    public static void showShell() {
        LoginView.stopMotion();
        shell = new ShellView();
        var desk = shell.root();
        stage.getScene().setRoot(desk);
        Fx.fadeIn(desk);
        shell.openFirst();
    }

    @Override
    public void start(Stage primary) {
        stage = primary;
        stage.setTitle(AppConfig.WINDOW_TITLE);
        stage.setMinWidth(1200);
        stage.setMinHeight(750);
        Label waiting = new Label("Opening the desk…");
        waiting.getStyleClass().add("section-title");
        StackPane splash = new StackPane(waiting);
        splash.getStyleClass().add("content");
        Scene scene = new Scene(splash, 1280, 800);
        var css = Main.class.getResource("/coralcove/theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setScene(scene);
        stage.show();
        Session.onDataChanged(() -> Platform.runLater(() -> {
            if (shell != null) {
                shell.refreshVisible();
            }
        }));
        Fx.run(() -> {
            DatabaseManager.init(AppConfig.databasePath());
            return null;
        }, ignored -> {
            var login = new LoginView().root();
            stage.getScene().setRoot(login);
            Fx.fadeIn(login);
        });
    }
}
