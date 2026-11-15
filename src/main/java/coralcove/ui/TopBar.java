package coralcove.ui;

import coralcove.Main;
import coralcove.model.Resort;
import coralcove.model.Role;
import coralcove.model.User;
import coralcove.service.ResortService;
import coralcove.service.Session;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class TopBar extends HBox {
    private final Label title = new Label("Dashboard");
    private final ComboBox<ResortChoice> resorts = new ComboBox<>();
    private boolean filling;

    public TopBar(Runnable onResortChanged) {
        getStyleClass().add("topbar");
        setAlignment(Pos.CENTER_LEFT);
        title.getStyleClass().add("section-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button password = new Button("Password");
        password.getStyleClass().add("secondary");
        password.setOnAction(event -> {
            User user = Session.get();
            if (user != null) {
                ChangePasswordDialog.show(user, false);
            }
        });
        Button logout = new Button("Log out");
        logout.getStyleClass().add("secondary");
        logout.setOnAction(event -> Main.showLogin());
        User user = Session.get();
        Label who = new Label(user == null ? "" : user.getUsername());
        who.getStyleClass().add("signed-in");
        if (user != null && user.getRole() == Role.ADMIN) {
            resorts.getItems().setAll(loadChoices());
            selectCurrent();
            resorts.valueProperty().addListener((obs, previous, choice) -> {
                if (filling || choice == null) {
                    return;
                }
                Session.setResortFilter(choice.id());
                onResortChanged.run();
            });
            getChildren().addAll(title, spacer, resorts, who, password, logout);
        } else {
            Label locked = new Label(resortLabel(user));
            locked.getStyleClass().add("muted");
            getChildren().addAll(title, spacer, locked, who, password, logout);
        }
    }

    public void setTitle(String text) {
        title.setText(text);
    }

    private void selectCurrent() {
        filling = true;
        Long filter = Session.resortFilter();
        for (ResortChoice choice : resorts.getItems()) {
            if (filter == null ? choice.id() == null : filter.equals(choice.id())) {
                resorts.getSelectionModel().select(choice);
                break;
            }
        }
        filling = false;
    }

    private static List<ResortChoice> loadChoices() {
        List<ResortChoice> choices = new ArrayList<>();
        for (Resort resort : new ResortService().list(true)) {
            choices.add(new ResortChoice(resort.getId(), resort.getName()));
        }
        choices.add(new ResortChoice(null, "All resorts"));
        return choices;
    }

    private static String resortLabel(User user) {
        if (user == null || user.getResortId() == null) {
            return "Resort";
        }
        for (Resort resort : new ResortService().list(false)) {
            if (resort.getId() == user.getResortId()) {
                return resort.getName();
            }
        }
        return "Resort";
    }

    public record ResortChoice(Long id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
