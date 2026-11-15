package coralcove.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public final class ScreenHeader extends StackPane {
    private final String heading;
    private final ImageView photo = new ImageView();
    private final Label title = new Label();
    private final Label hook = new Label();
    private final Rectangle clip = new Rectangle();

    public ScreenHeader(String heading, String line) {
        this.heading = heading;
        getStyleClass().add("screen-header");
        setMinHeight(200);
        setPrefHeight(220);
        setMaxHeight(260);

        clip.setArcWidth(16);
        clip.setArcHeight(16);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        photo.setPreserveRatio(true);
        photo.setSmooth(true);
        photo.setImage(ResortArt.forScreen(heading));

        Region veil = new Region();
        veil.getStyleClass().add("header-veil");
        veil.prefWidthProperty().bind(widthProperty());
        veil.prefHeightProperty().bind(heightProperty());

        title.setText(heading);
        title.getStyleClass().add("header-title");
        hook.setText(line);
        hook.getStyleClass().add("header-hook");
        hook.setWrapText(true);

        VBox copy = new VBox(8, title, hook);
        copy.setAlignment(Pos.CENTER_LEFT);
        copy.setPadding(new Insets(24, 30, 24, 30));

        getChildren().addAll(photo, veil, copy);
        StackPane.setAlignment(photo, Pos.CENTER);
        StackPane.setAlignment(copy, Pos.CENTER_LEFT);

        widthProperty().addListener((obs, o, n) -> updatePhotoLayout());
        heightProperty().addListener((obs, o, n) -> updatePhotoLayout());
        photo.imageProperty().addListener((obs, o, n) -> updatePhotoLayout());
    }

    @Override
    protected void layoutChildren() {
        updatePhotoLayout();
        super.layoutChildren();
    }

    private void updatePhotoLayout() {
        Image img = photo.getImage();
        if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
            return;
        }
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        double imgWidth = img.getWidth();
        double imgHeight = img.getHeight();
        double scale = Math.max(w / imgWidth, h / imgHeight);
        photo.setFitWidth(imgWidth * scale);
        photo.setFitHeight(imgHeight * scale);
    }

    public void setHook(String line) {
        hook.setText(line);
    }

    public void setPhoto(Image image) {
        if (image != null) {
            photo.setImage(image);
            updatePhotoLayout();
        }
    }

    public void refreshPhoto() {
        photo.setImage(ResortArt.forScreen(heading));
        updatePhotoLayout();
    }
}
