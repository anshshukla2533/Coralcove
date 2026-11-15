package coralcove.ui;

import coralcove.Main;
import coralcove.model.Role;
import coralcove.model.User;
import coralcove.service.AuthenticationService;
import coralcove.service.ResortService;
import coralcove.service.Session;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class ProfileDialog {

    private ProfileDialog() {
    }

    public static void show(User user, Runnable onSaved) {
        if (user == null) {
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (Main.stage() != null) {
            dialog.initOwner(Main.stage());
        }
        dialog.setTitle("User Profile · " + user.getEffectiveName());
        dialog.setMinWidth(520);
        dialog.setMinHeight(560);
        dialog.setResizable(true);

        // Header
        Label headerTitle = new Label("Profile Details");
        headerTitle.getStyleClass().add("section-title");
        Label headerSub = new Label("Manage your account information, role identity, and photo avatar.");
        headerSub.getStyleClass().add("muted");
        VBox headerBox = new VBox(4, headerTitle, headerSub);
        headerBox.setPadding(new Insets(18, 24, 14, 24));
        headerBox.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #E4DDD2 transparent;");

        // Avatar State
        final String[] activeAvatarPath = {user.getAvatarPath()};
        StackPane avatarHolder = new StackPane();
        Runnable refreshAvatarView = () -> {
            User temp = new User();
            temp.setUsername(user.getUsername());
            temp.setDisplayName(user.getDisplayName());
            temp.setAvatarPath(activeAvatarPath[0]);
            avatarHolder.getChildren().setAll(AvatarUtils.avatarNode(temp, 100));
        };
        refreshAvatarView.run();

        Button uploadBtn = new Button("Upload photo");
        uploadBtn.getStyleClass().add("secondary");
        uploadBtn.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose Profile Photo");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg, *.webp)", "*.png", "*.jpg", "*.jpeg", "*.webp")
            );
            File file = chooser.showOpenDialog(dialog);
            if (file != null) {
                try {
                    String savedPath = AvatarUtils.saveUploadedAvatar(user.getId(), file);
                    activeAvatarPath[0] = savedPath;
                    refreshAvatarView.run();
                } catch (Exception ex) {
                    Fx.error(ex);
                }
            }
        });

        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().add("secondary");
        removeBtn.setOnAction(event -> {
            activeAvatarPath[0] = null;
            refreshAvatarView.run();
        });

        // Preset Avatars
        Label presetLabel = new Label("Or pick a style:");
        presetLabel.setStyle("-fx-text-fill: #8C7338; -fx-font-size: 11px;");
        HBox presets = new HBox(8);
        presets.setAlignment(Pos.CENTER_LEFT);
        String[] presetNames = {"avatar-owner.png", "avatar-manager.png", "avatar-frontdesk.png"};
        String[] presetTooltips = {"Owner / Director", "Manager", "Front Desk"};
        for (int i = 0; i < presetNames.length; i++) {
            String pName = presetNames[i];
            String pTip = presetTooltips[i];
            User presetUser = new User();
            presetUser.setUsername(pTip);
            presetUser.setAvatarPath(pName);
            var node = AvatarUtils.avatarNode(presetUser, 34);
            node.setStyle("-fx-cursor: hand;");
            node.setOnMouseClicked(e -> {
                activeAvatarPath[0] = pName;
                refreshAvatarView.run();
            });
            presets.getChildren().add(node);
        }

        VBox avatarControls = new VBox(6,
                new HBox(8, uploadBtn, removeBtn),
                presetLabel,
                presets
        );
        avatarControls.setAlignment(Pos.CENTER_LEFT);

        HBox avatarSection = new HBox(18, avatarHolder, avatarControls);
        avatarSection.setAlignment(Pos.CENTER_LEFT);
        avatarSection.setPadding(new Insets(12, 0, 16, 0));

        // Form Fields
        TextField displayNameField = new TextField(user.getDisplayName() == null ? "" : user.getDisplayName());
        displayNameField.setPromptText("e.g. Aniket Sharma (Owner)");

        TextField emailField = new TextField(user.getEmail() == null ? "" : user.getEmail());
        emailField.setPromptText("e.g. aniket@coralcove.resorts");

        TextField phoneField = new TextField(user.getPhone() == null ? "" : user.getPhone());
        phoneField.setPromptText("e.g. +91 98765 43210");

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B2430;");

        Label roleBadge = Fx.chip(user.getRole() == Role.ADMIN ? "OWNER / ADMIN" : user.getRole().label(),
                user.getRole() == Role.ADMIN ? "vip" : "ready");

        Label resortLabel = new Label(resortName(user));
        resortLabel.getStyleClass().add("muted");

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);

        int row = 0;
        form.add(new Label("Username"), 0, row);
        form.add(usernameLabel, 1, row++);

        form.add(new Label("Role & Permission"), 0, row);
        form.add(roleBadge, 1, row++);

        form.add(new Label("Resort Assigned"), 0, row);
        form.add(resortLabel, 1, row++);

        form.add(new Label("Display Name"), 0, row);
        form.add(displayNameField, 1, row++);

        form.add(new Label("Email Address"), 0, row);
        form.add(emailField, 1, row++);

        form.add(new Label("Contact Phone"), 0, row);
        form.add(phoneField, 1, row++);

        Button changePassBtn = new Button("Change password…");
        changePassBtn.getStyleClass().add("secondary");
        changePassBtn.setOnAction(event -> ChangePasswordDialog.show(user, false));
        form.add(new Label("Security"), 0, row);
        form.add(changePassBtn, 1, row++);

        VBox contentBox = new VBox(12, avatarSection, form);
        contentBox.setPadding(new Insets(16, 24, 20, 24));

        ScrollPane scroller = new ScrollPane(contentBox);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.getStyleClass().add("desk-scroll");
        scroller.setStyle("-fx-background-color: transparent; -fx-background: #FAF8F5; -fx-padding: 0;");

        // Buttons
        Button saveBtn = new Button("Save Profile");
        saveBtn.getStyleClass().add("primary");
        saveBtn.setDefaultButton(true);
        saveBtn.setMinWidth(110);
        saveBtn.setOnAction(event -> {
            String name = displayNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String avatar = activeAvatarPath[0];

            Fx.run(() -> {
                new AuthenticationService().updateProfile(user.getId(), name, email, phone, avatar);
                return null;
            }, ignored -> {
                if (onSaved != null) {
                    onSaved.run();
                }
                dialog.close();
            });
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("secondary");
        cancelBtn.setCancelButton(true);
        cancelBtn.setMinWidth(80);
        cancelBtn.setOnAction(event -> dialog.close());

        HBox buttonBar = new HBox(12, cancelBtn, saveBtn);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(14, 24, 16, 24));
        buttonBar.setStyle("-fx-background-color: #F7F4EF; -fx-border-color: #E4DDD2 transparent transparent transparent; -fx-border-width: 1;");

        BorderPane root = new BorderPane();
        root.setTop(headerBox);
        root.setCenter(scroller);
        root.setBottom(buttonBar);

        Scene scene = new Scene(root, 560, 680);
        var css = ProfileDialog.class.getResource("/coralcove/theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static String resortName(User user) {
        if (user.getRole() == Role.ADMIN) {
            return "All Properties (Owner / Group Level)";
        }
        if (user.getResortId() == null) {
            return "Unassigned";
        }
        for (var resort : new ResortService().list(false)) {
            if (resort.getId() == user.getResortId()) {
                return resort.getName() + " (" + resort.getLocation() + ")";
            }
        }
        return "Resort #" + user.getResortId();
    }
}
