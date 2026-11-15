package coralcove.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class ScreenHeader extends StackPane {
    private final String heading;
    private final ImageView photo = new ImageView();
    private final Label title = new Label();
    private final Label hook = new Label();

    public ScreenHeader(String heading, String line) {
        this.heading = heading;
        getStyleClass().add("screen-header");
        setMinHeight(128);
        setPrefHeight(128);
        setMaxHeight(148);
        photo.setPreserveRatio(false);
        photo.fitWidthProperty().bind(widthProperty());
        photo.fitHeightProperty().bind(heightProperty());
        photo.setImage(ResortArt.forScreen(heading));
        Region veil = new Region();
        veil.getStyleClass().add("header-veil");
        title.setText(heading);
        title.getStyleClass().add("header-title");
        hook.setText(line);
        hook.getStyleClass().add("header-hook");
        hook.setWrapText(true);
        VBox copy = new VBox(6, title, hook);
        copy.setAlignment(Pos.CENTER_LEFT);
        copy.setPadding(new Insets(18, 22, 18, 22));
        getChildren().addAll(photo, veil, copy);
        StackPane.setAlignment(copy, Pos.CENTER_LEFT);
    }

    public void setHook(String line) {
        hook.setText(line);
    }

    public void setPhoto(Image image) {
        if (image != null) {
            photo.setImage(image);
        }
    }

    public void refreshPhoto() {
        photo.setImage(ResortArt.forScreen(heading));
    }
}
