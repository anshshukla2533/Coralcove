package coralcove.ui.settings;

import coralcove.model.Resort;
import coralcove.model.Role;
import coralcove.model.User;
import coralcove.service.AuthenticationService;
import coralcove.service.DemoDataService;
import coralcove.service.ResortService;
import coralcove.ui.Columns;
import coralcove.ui.Dialogs;
import coralcove.ui.Fx;
import coralcove.ui.ProfileDialog;
import coralcove.ui.Refreshable;
import coralcove.ui.ResortArt;
import coralcove.ui.ScreenHeader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import coralcove.util.AppException;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class SettingsView extends VBox implements Refreshable {
    private final ResortService resorts = new ResortService();
    private final AuthenticationService auth = new AuthenticationService();
    private final DemoDataService demo = new DemoDataService();
    private final TableView<Resort> resortTable = new TableView<>();
    private final TableView<User> userTable = new TableView<>();
    private final FlowPane photos = new FlowPane(10, 10);
    private final HBox myProfileCard = new HBox(16);
    private final ScreenHeader header = new ScreenHeader("Settings", "Resorts, users, and sample data.");

    public SettingsView() {
        setSpacing(10);
        myProfileCard.setAlignment(Pos.CENTER_LEFT);
        myProfileCard.setPadding(new Insets(14, 18, 14, 18));
        myProfileCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E4DDD2; -fx-border-radius: 8;");
        resortTable.getColumns().add(Columns.of("Name", Resort::getName));
        resortTable.getColumns().add(Columns.of("Location", Resort::getLocation));
        resortTable.getColumns().add(Columns.of("Zone", Resort::getTimezone));
        resortTable.getColumns().add(Columns.of("Tax %", Resort::getTaxPercent));
        resortTable.getColumns().add(Columns.of("Active", resort -> resort.isActive() ? "Yes" : "No"));
        resortTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        userTable.getColumns().add(Columns.of("Username", User::getUsername));
        userTable.getColumns().add(Columns.of("Name", User::getEffectiveName));
        userTable.getColumns().add(Columns.of("Role", user -> user.getRole().label()));
        userTable.getColumns().add(Columns.of("Resort", user -> user.getResortId() == null ? "All" : String.valueOf(user.getResortId())));
        userTable.getColumns().add(Columns.of("Active", user -> user.isActive() ? "Yes" : "No"));
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Button editResort = button("Edit resort", false);
        editResort.setOnAction(event -> editResort());
        Button newResort = button("New resort", true);
        newResort.setOnAction(event -> editResort(null));
        Button newUser = button("New user", true);
        newUser.setOnAction(event -> newUser());
        Button editUser = button("Edit user", false);
        editUser.setOnAction(event -> editUser());
        Button load = button("Load sample", false);
        load.setOnAction(event -> {
            if (Fx.confirm("Load the sample resorts, stays, and the beach.desk account?")) {
                Fx.run(() -> {
                    demo.load();
                    return null;
                }, ignored -> refresh());
            }
        });
        Button reset = button("Reset sample", false);
        reset.setOnAction(event -> {
            if (Fx.confirm("Reset removes sample rows, the Cliff resort, and beach.desk. Beach and the administrator remain.")) {
                Fx.run(() -> {
                    demo.reset();
                    return null;
                }, ignored -> refresh());
            }
        });
        VBox.setVgrow(resortTable, Priority.ALWAYS);
        userTable.setPrefHeight(200);
        ScrollPane scroller = new ScrollPane(photos);
        scroller.setFitToWidth(true);
        scroller.setPrefHeight(140);
        photos.setPadding(new Insets(4));
        getChildren().addAll(
                header,
                new Label("My Profile"),
                myProfileCard,
                scroller,
                new Label("Resorts"),
                Fx.bar(newResort, editResort),
                resortTable,
                new Label("Users"),
                Fx.bar(newUser, editUser),
                userTable,
                Fx.bar(load, reset));
    }

    @Override
    public void refresh() {
        renderProfileCard();
        Fx.run(() -> new Object[] {resorts.list(false), auth.listUsers()}, loaded -> {
            java.util.List<Resort> properties = (java.util.List<Resort>) loaded[0];
            resortTable.getItems().setAll(properties);
            userTable.getItems().setAll((java.util.List<User>) loaded[1]);
            photos.getChildren().clear();
            for (Resort resort : properties) {
                photos.getChildren().add(card(resort));
            }
            header.refreshPhoto();
            header.setHook(properties.size() + " resorts  ·  " + ((java.util.List<?>) loaded[1]).size() + " accounts");
        });
    }

    private void renderProfileCard() {
        myProfileCard.getChildren().clear();
        User current = coralcove.service.Session.get();
        if (current == null) {
            return;
        }
        var avatar = coralcove.ui.AvatarUtils.avatarNode(current, 56);

        Label name = new Label(current.getEffectiveName());
        name.getStyleClass().add("panel-title");

        String subText = "@" + current.getUsername() + "  ·  "
                + (current.getEmail() != null && !current.getEmail().isBlank() ? current.getEmail() : "No email set")
                + "  ·  "
                + (current.getPhone() != null && !current.getPhone().isBlank() ? current.getPhone() : "No phone set");
        Label details = new Label(subText);
        details.getStyleClass().add("muted");

        var roleChip = Fx.chip(current.getRole() == Role.ADMIN ? "Owner / Administrator" : current.getRole().label(),
                current.getRole() == Role.ADMIN ? "vip" : "ready");

        VBox info = new VBox(4, new HBox(8, name, roleChip), details);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button editProfileBtn = button("Edit Profile & Photo", true);
        editProfileBtn.setOnAction(e -> ProfileDialog.show(current, this::refresh));

        myProfileCard.getChildren().addAll(avatar, info, editProfileBtn);
    }

    private void editResort() {
        editResort(resortTable.getSelectionModel().getSelectedItem());
    }

    private void editResort(Resort existing) {
        if (existing == null && resortTable.getScene() == null) {
            return;
        }
        TextField name = new TextField(existing == null ? "" : existing.getName());
        TextField location = new TextField(existing == null ? "" : existing.getLocation());
        TextField coastline = new TextField(existing == null ? "" : empty(existing.getCoastline()));
        TextField zone = new TextField(existing == null ? "Asia/Kolkata" : existing.getTimezone());
        TextField tax = new TextField(existing == null ? "12" : existing.getTaxPercent());
        CheckBox active = new CheckBox("Active");
        active.setSelected(existing == null || existing.isActive());
        GridPane form = new GridPane();
        form.add(new Label("Name"), 0, 0);
        form.add(name, 1, 0);
        form.add(new Label("Location"), 0, 1);
        form.add(location, 1, 1);
        form.add(new Label("Coastline"), 0, 2);
        form.add(coastline, 1, 2);
        form.add(new Label("Time zone"), 0, 3);
        form.add(zone, 1, 3);
        form.add(new Label("Tax percent"), 0, 4);
        form.add(tax, 1, 4);
        form.add(active, 1, 5);
        Resort[] draft = new Resort[1];
        Dialogs.form(existing == null ? "Resort" : "Edit resort", form, () -> {
            Resort resort = existing == null ? new Resort() : existing;
            resort.setName(name.getText());
            resort.setLocation(location.getText());
            resort.setCoastline(coastline.getText());
            resort.setTimezone(zone.getText());
            resort.setTaxPercent(tax.getText());
            resort.setActive(active.isSelected());
            draft[0] = resort;
        });
        if (draft[0] != null) {
            Fx.run(() -> resorts.save(draft[0]), saved -> refresh());
        }
    }

    private void newUser() {
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        ComboBox<Role> role = new ComboBox<>();
        role.getItems().setAll(Role.values());
        role.getSelectionModel().select(Role.FRONT_DESK);
        GridPane form = new GridPane();
        form.add(new Label("Username"), 0, 0);
        form.add(username, 1, 0);
        form.add(new Label("Password"), 0, 1);
        form.add(password, 1, 1);
        form.add(new Label("Role"), 0, 2);
        form.add(role, 1, 2);
        String[] picked = new String[1];
        Dialogs.form("New user", form, () -> {
            if (role.getValue() == null) {
                throw new AppException("Role is required.");
            }
            picked[0] = username.getText() + "\n" + password.getText() + "\n" + role.getValue().name();
        });
        if (picked[0] == null) {
            return;
        }
        String[] parts = picked[0].split("\n", 3);
        Fx.run(() -> {
            auth.createUser(parts[0], parts[1], Role.parse(parts[2]));
            return null;
        }, ignored -> refresh());
    }

    private void editUser() {
        User user = userTable.getSelectionModel().getSelectedItem();
        if (user == null) {
            return;
        }
        Fx.run(() -> resorts.list(true), properties -> {
            ComboBox<Role> role = new ComboBox<>();
            role.getItems().setAll(Role.values());
            role.getSelectionModel().select(user.getRole());
            ComboBox<Resort> property = new ComboBox<>();
            property.getItems().setAll(properties);
            properties.stream().filter(item -> user.getResortId() != null && item.getId() == user.getResortId())
                    .findFirst().ifPresent(property.getSelectionModel()::select);
            CheckBox active = new CheckBox("Active");
            active.setSelected(user.isActive());
            GridPane form = new GridPane();
            form.add(new Label(user.getUsername()), 0, 0, 2, 1);
            form.add(new Label("Role"), 0, 1);
            form.add(role, 1, 1);
            form.add(new Label("Resort"), 0, 2);
            form.add(property, 1, 2);
            form.add(active, 1, 3);
            User[] draft = new User[1];
            Dialogs.form("Edit user", form, () -> {
                user.setRole(role.getValue());
                user.setActive(active.isSelected());
                if (role.getValue() == Role.ADMIN) {
                    user.setResortId(null);
                } else if (property.getValue() == null) {
                    throw new AppException("Assign this account to a resort.");
                } else {
                    user.setResortId(property.getValue().getId());
                }
                draft[0] = user;
            });
            if (draft[0] != null) {
                Fx.run(() -> {
                    auth.updateUser(draft[0]);
                    return null;
                }, ignored -> refresh());
            }
        });
    }

    private static StackPane card(Resort resort) {
        ImageView photo = new ImageView(ResortArt.forResort(resort));
        photo.setPreserveRatio(true);
        photo.setSmooth(true);
        var img = photo.getImage();
        if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
            double scale = Math.max(220.0 / img.getWidth(), 120.0 / img.getHeight());
            photo.setFitWidth(img.getWidth() * scale);
            photo.setFitHeight(img.getHeight() * scale);
        }
        Region veil = new Region();
        veil.setStyle("-fx-background-color: linear-gradient(from 0% 40% to 0% 100%, transparent, rgba(20,26,34,0.9));");
        Label name = new Label(resort.getName());
        name.setStyle("-fx-text-fill: #F7F4EF; -fx-font-weight: 700;");
        Label place = new Label(resort.getLocation() == null ? "" : resort.getLocation());
        place.setStyle("-fx-text-fill: #C4A35A;");
        VBox copy = new VBox(2, name, place);
        copy.setPadding(new Insets(10));
        copy.setAlignment(Pos.BOTTOM_LEFT);
        StackPane card = new StackPane(photo, veil, copy);
        card.getStyleClass().add("photo-card");
        card.setPrefWidth(220);
        card.setPrefHeight(120);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(220, 120);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        card.setClip(clip);
        StackPane.setAlignment(copy, Pos.BOTTOM_LEFT);
        return card;
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
