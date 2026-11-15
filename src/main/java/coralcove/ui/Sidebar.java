package coralcove.ui;

import coralcove.model.AppModule;
import coralcove.model.Role;
import coralcove.service.AuthorizationService;
import coralcove.service.Session;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public final class Sidebar extends VBox {
    private final Map<AppModule, Button> buttons = new EnumMap<>(AppModule.class);
    private AppModule selected;

    public Sidebar(Consumer<AppModule> onSelect) {
        getStyleClass().add("sidebar");

        ImageView logoView = new ImageView(ResortArt.icon(40));
        logoView.setFitWidth(36);
        logoView.setFitHeight(36);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        Rectangle logoClip = new Rectangle(36, 36);
        logoClip.setArcWidth(10);
        logoClip.setArcHeight(10);
        logoView.setClip(logoClip);

        Label brand = new Label("CoralCove");
        brand.getStyleClass().add("brand");
        brand.setStyle("-fx-padding: 0;");
        Label desk = new Label("Resort desk");
        desk.getStyleClass().add("brand-sub");
        desk.setStyle("-fx-padding: 0;");

        VBox brandCopy = new VBox(2, brand, desk);
        HBox brandHeader = new HBox(10, logoView, brandCopy);
        brandHeader.setAlignment(Pos.CENTER_LEFT);
        brandHeader.setPadding(new Insets(10, 10, 8, 12));

        Region rule = new Region();
        rule.getStyleClass().add("gold-rule");
        getChildren().addAll(brandHeader, rule);
        Role role = Session.get() == null ? null : Session.get().getRole();
        for (AppModule module : AppModule.values()) {
            if (!AuthorizationService.allows(role, module)) {
                continue;
            }
            Button button = new Button(module.label());
            button.getStyleClass().add("nav-button");
            button.setOnAction(event -> onSelect.accept(module));
            buttons.put(module, button);
            getChildren().add(button);
        }
    }

    public void select(AppModule module) {
        selected = module;
        buttons.forEach((key, button) -> {
            if (key == module) {
                if (!button.getStyleClass().contains("selected")) {
                    button.getStyleClass().add("selected");
                }
            } else {
                button.getStyleClass().remove("selected");
            }
        });
    }

    public AppModule first() {
        return buttons.keySet().stream().findFirst().orElse(AppModule.DASHBOARD);
    }
}
