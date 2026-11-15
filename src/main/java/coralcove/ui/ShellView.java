package coralcove.ui;

import coralcove.model.AppModule;
import coralcove.ui.arrivals.ArrivalView;
import coralcove.ui.billing.BillingView;
import coralcove.ui.dashboard.DashboardView;
import coralcove.ui.employees.EmployeeView;
import coralcove.ui.experiences.ExperienceView;
import coralcove.ui.guests.GuestView;
import coralcove.ui.inventory.InventoryView;
import coralcove.ui.lodgecare.LodgeCareView;
import coralcove.ui.lodges.LodgeView;
import coralcove.ui.reports.ReportsView;
import coralcove.ui.settings.SettingsView;
import coralcove.ui.stays.StayView;
import java.util.EnumMap;
import java.util.Map;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public final class ShellView {
    private final BorderPane root = new BorderPane();
    private final StackPane center = new StackPane();
    private final ScrollPane scroll = new ScrollPane();
    private final Map<AppModule, Refreshable> views = new EnumMap<>(AppModule.class);
    private final Map<AppModule, Parent> nodes = new EnumMap<>(AppModule.class);
    private final TopBar topBar;
    private final Sidebar sidebar;
    private AppModule visible;

    public ShellView() {
        topBar = new TopBar(this::refreshVisible);
        sidebar = new Sidebar(this::show);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("desk-scroll");
        center.getChildren().add(scroll);
        root.getStyleClass().add("shell");
        root.setLeft(sidebar);
        root.setTop(topBar);
        root.setCenter(center);
        center.getStyleClass().add("content");
    }

    public Parent root() {
        return root;
    }

    public void openFirst() {
        show(sidebar.first());
    }

    public void refreshVisible() {
        if (visible != null) {
            views.get(visible).refresh();
        }
    }

    private void show(AppModule module) {
        boolean same = module == visible;
        visible = module;
        sidebar.select(module);
        topBar.setTitle(module.label());
        Parent node = nodes.computeIfAbsent(module, this::create);
        scroll.setContent(node);
        views.get(module).refresh();
        if (!same) {
            Fx.fadeIn(node);
        }
    }

    private Parent create(AppModule module) {
        Refreshable view = switch (module) {
            case DASHBOARD -> new DashboardView();
            case STAYS -> new StayView();
            case LODGES -> new LodgeView();
            case GUESTS -> new GuestView();
            case ARRIVALS -> new ArrivalView();
            case BILLING -> new BillingView();
            case LODGE_CARE -> new LodgeCareView();
            case EXPERIENCES -> new ExperienceView();
            case EMPLOYEES -> new EmployeeView();
            case INVENTORY -> new InventoryView();
            case REPORTS -> new ReportsView();
            case SETTINGS -> new SettingsView();
        };
        views.put(module, view);
        if (view instanceof Parent parent) {
            parent.getStyleClass().add("screen");
            return parent;
        }
        throw new IllegalStateException(module.name());
    }
}
