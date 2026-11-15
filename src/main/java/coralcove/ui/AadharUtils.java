package coralcove.ui;

import coralcove.util.AppException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class AadharUtils {

    private AadharUtils() {
    }

    public static Image loadAadharImage(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            if ("sample-aadhar.png".equalsIgnoreCase(path) || path.contains("sample-aadhar")) {
                var url = AadharUtils.class.getResource("/coralcove/media/sample-aadhar.png");
                if (url != null) {
                    return new Image(url.toExternalForm());
                }
            }
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                return new Image(file.toURI().toString());
            }
            var res = AadharUtils.class.getResource("/coralcove/media/" + path);
            if (res != null) {
                return new Image(res.toExternalForm());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String saveUploadedAadhar(long guestId, File chosenFile) {
        try {
            Path targetDir = Path.of("data", "aadhar");
            Files.createDirectories(targetDir);

            String ext = "png";
            String originalName = chosenFile.getName();
            int dot = originalName.lastIndexOf('.');
            if (dot > 0 && dot < originalName.length() - 1) {
                ext = originalName.substring(dot + 1).toLowerCase();
            }

            String filename = "aadhar_guest_" + guestId + "_" + System.currentTimeMillis() + "." + ext;
            Path destination = targetDir.resolve(filename);
            Files.copy(chosenFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toString();
        } catch (IOException ex) {
            throw new AppException("Failed to save Aadhaar image: " + ex.getMessage(), ex);
        }
    }

    public static Node createThumbnail(String aadharImagePath, double width, double height, Runnable onClick) {
        Image img = loadAadharImage(aadharImagePath);
        if (img == null) {
            Label noDoc = new Label("No card uploaded");
            noDoc.setStyle("-fx-text-fill: #8C7338; -fx-font-size: 11px;");
            StackPane box = new StackPane(noDoc);
            box.setPrefSize(width, height);
            box.setStyle("-fx-background-color: #F7F4EF; -fx-border-color: #E4DDD2; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-style: dashed;");
            return box;
        }

        ImageView iv = new ImageView(img);
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        Label overlay = new Label("🔍 Click to view");
        overlay.setStyle("-fx-text-fill: white; -fx-background-color: rgba(20,26,34,0.75); -fx-padding: 3 6; -fx-font-size: 10px; -fx-background-radius: 4;");
        StackPane.setAlignment(overlay, Pos.BOTTOM_CENTER);
        StackPane.setMargin(overlay, new Insets(0, 0, 4, 0));

        StackPane pane = new StackPane(iv, overlay);
        pane.setPrefSize(width, height);
        pane.setMaxSize(width, height);
        pane.setStyle("-fx-cursor: hand; -fx-border-color: #C4A35A; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-background-color: #FFFFFF;");
        if (onClick != null) {
            pane.setOnMouseClicked(e -> onClick.run());
        }
        return pane;
    }

    public static void showViewer(Stage owner, String title, Image image) {
        if (image == null) {
            return;
        }
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle(title != null ? title : "Aadhaar Card Viewer");
        dialog.setMinWidth(550);
        dialog.setMinHeight(400);

        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitWidth(650);

        ScrollPane scroller = new ScrollPane(view);
        scroller.setPannable(true);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background-color: #141A22;");

        Label heading = new Label(title != null ? title : "Aadhaar Card Identification Proof");
        heading.getStyleClass().add("panel-title");

        Button close = new Button("Close");
        close.getStyleClass().add("secondary");
        close.setOnAction(e -> dialog.close());

        HBox top = new HBox(12, heading);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(14, 18, 14, 18));
        top.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #E4DDD2 transparent;");

        HBox bottom = new HBox(close);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(12, 18, 12, 18));
        bottom.setStyle("-fx-background-color: #F7F4EF; -fx-border-color: #E4DDD2 transparent transparent transparent;");

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(scroller);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 700, 480);
        var css = AadharUtils.class.getResource("/coralcove/theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
