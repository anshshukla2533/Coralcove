package coralcove.ui;

import coralcove.model.User;
import coralcove.util.AppException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public final class AvatarUtils {

    private AvatarUtils() {
    }

    public static Image loadAvatar(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            // Check if it's a bundled preset avatar
            if (path.startsWith("avatar-") || path.contains("avatar")) {
                String clean = path.replace(".png", "").replace(".jpg", "");
                Image img = ResortArt.image(clean);
                if (img != null) {
                    return img;
                }
            }
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                return new Image(file.toURI().toString(), 256, 256, true, true);
            }
            var res = AvatarUtils.class.getResource("/coralcove/media/" + path);
            if (res != null) {
                return new Image(res.toExternalForm(), 256, 256, true, true);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Node avatarNode(User user, double size) {
        String avatarPath = user == null ? null : user.getAvatarPath();
        Image img = loadAvatar(avatarPath);
        if (img != null) {
            ImageView view = new ImageView(img);
            view.setPreserveRatio(true);
            view.setSmooth(true);
            double w = img.getWidth();
            double h = img.getHeight();
            if (w > 0 && h > 0) {
                double scale = Math.max(size / w, size / h);
                view.setFitWidth(w * scale);
                view.setFitHeight(h * scale);
            } else {
                view.setFitWidth(size);
                view.setFitHeight(size);
            }

            StackPane photoHolder = new StackPane(view);
            photoHolder.setMinSize(size, size);
            photoHolder.setPrefSize(size, size);
            photoHolder.setMaxSize(size, size);
            Circle clip = new Circle(size / 2, size / 2, size / 2);
            photoHolder.setClip(clip);

            Circle ring = new Circle(size / 2);
            ring.setFill(null);
            ring.setStyle("-fx-stroke: #C4A35A; -fx-stroke-width: 1.5;");

            StackPane container = new StackPane(photoHolder, ring);
            container.setMaxSize(size, size);
            container.setMinSize(size, size);
            container.setPrefSize(size, size);
            return container;
        }

        // Fallback initials avatar
        String name = user == null ? "U" : user.getEffectiveName();
        String initials = initials(name);

        Circle circle = new Circle(size / 2);
        circle.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #1F2A38, #141A22); "
                + "-fx-stroke: #C4A35A; -fx-stroke-width: 1.5;");

        Label label = new Label(initials);
        double fontSize = Math.max(10, size * 0.4);
        label.setStyle("-fx-text-fill: #E4DDD2; -fx-font-weight: bold; -fx-font-size: " + fontSize + "px;");

        StackPane pane = new StackPane(circle, label);
        pane.setAlignment(Pos.CENTER);
        pane.setMaxSize(size, size);
        pane.setMinSize(size, size);
        pane.setPrefSize(size, size);
        return pane;
    }

    public static String saveUploadedAvatar(long userId, File chosenFile) {
        try {
            Path targetDir = Path.of("data", "avatars");
            Files.createDirectories(targetDir);

            String ext = "png";
            String originalName = chosenFile.getName();
            int dot = originalName.lastIndexOf('.');
            if (dot > 0 && dot < originalName.length() - 1) {
                ext = originalName.substring(dot + 1).toLowerCase();
            }

            String filename = "avatar_user_" + userId + "_" + System.currentTimeMillis() + "." + ext;
            Path destination = targetDir.resolve(filename);
            Files.copy(chosenFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toString();
        } catch (IOException ex) {
            throw new AppException("Failed to save avatar image: " + ex.getMessage(), ex);
        }
    }

    public static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "CC";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
