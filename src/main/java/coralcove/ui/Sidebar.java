package coralcove.ui;

import coralcove.model.AppModule;
import coralcove.model.Role;
import coralcove.service.AuthorizationService;
import coralcove.service.Session;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class Sidebar extends VBox {
    private final Map<AppModule, Button> buttons = new EnumMap<>(AppModule.class);
    private AppModule selected;

    public Sidebar(Consumer<AppModule> onSelect) {
        getStyleClass().add("sidebar");
        Label brand = new Label("CoralCove");
        brand.getStyleClass().add("brand");
        Label desk = new Label("Resort desk");
        desk.getStyleClass().add("brand-sub");
        Region rule = new Region();
        rule.getStyleClass().add("gold-rule");
        getChildren().addAll(brand, desk, rule);
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
