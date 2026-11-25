package coralcove.ui.experiences;

import coralcove.model.Experience;
import coralcove.model.ExperienceBooking;
import coralcove.model.Lodge;
import coralcove.service.ExperienceService;
import coralcove.service.LodgeService;
import coralcove.ui.Columns;
import coralcove.ui.Dialogs;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ResortArt;
import coralcove.ui.ScreenHeader;
import coralcove.util.AppException;
import coralcove.util.CurrencyUtils;
import coralcove.util.Money;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class ExperienceView extends VBox implements Refreshable {
    private static final String[] ORDER_STATUS = {"NEW", "PREPARING", "READY", "SERVED", "CANCELLED"};
    private final ExperienceService experiences = new ExperienceService();
    private final ComboBox<String> status = new ComboBox<>();
    private final TableView<Experience> menu = new TableView<>();
    private final TableView<ExperienceBooking> orders = new TableView<>();
    private final FlowPane tiles = new FlowPane(10, 10);
    private final ScreenHeader header = new ScreenHeader("Experiences", "Dining, spa, and water.");

    public ExperienceView() {
        setSpacing(10);
        status.getItems().add("");
        status.getItems().addAll(ORDER_STATUS);
        status.getSelectionModel().select(0);
        menu.getColumns().add(Columns.of("Category", Experience::getCategory));
        menu.getColumns().add(Columns.of("Item", Experience::getName));
        menu.getColumns().add(Columns.of("Price", item -> CurrencyUtils.format(item.getPrice())));
        menu.getColumns().add(Columns.of("Active", item -> item.isActive() ? "Yes" : "No"));
        menu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        orders.getColumns().add(Columns.of("Type", ExperienceBooking::getOrderType));
        orders.getColumns().add(Columns.of("Lodge", ExperienceBooking::getUnitCode));
        orders.getColumns().add(Columns.of("Item", ExperienceBooking::getDescription));
        orders.getColumns().add(Columns.of("Qty", order -> String.valueOf(order.getQty())));
        orders.getColumns().add(Columns.of("Total", order -> CurrencyUtils.format(order.getLineTotal())));
        orders.getColumns().add(Columns.of("Status", ExperienceBooking::getStatus));
        orders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Button refresh = button("Refresh", false);
        refresh.setOnAction(event -> refresh());
        Button add = button("Add menu item", false);
        add.setOnAction(event -> addItem());
        Button order = button("New order", true);
        order.setOnAction(event -> place());
        Button advance = button("Update status", false);
        advance.setOnAction(event -> advance());
        ScrollPane scroller = new ScrollPane(tiles);
        scroller.setFitToWidth(true);
        scroller.setMinHeight(160);
        scroller.setPrefHeight(175);
        tiles.setPadding(new Insets(4));
        VBox.setVgrow(menu, Priority.ALWAYS);
        menu.setMinHeight(190);
        menu.setPrefHeight(220);
        VBox.setVgrow(orders, Priority.ALWAYS);
        orders.setMinHeight(200);
        orders.setPrefHeight(230);
        getChildren().addAll(header, Fx.bar(status, refresh, add, order, advance), scroller,
                new Label("Catalog"), menu, new Label("Orders"), orders);
    }

    @Override
    public void refresh() {
        String filter = status.getValue();
        Fx.run(() -> new Object[] {experiences.menu(), experiences.orders(filter)}, loaded -> {
            java.util.List<Experience> catalog = (java.util.List<Experience>) loaded[0];
            java.util.List<ExperienceBooking> booked = (java.util.List<ExperienceBooking>) loaded[1];
            menu.getItems().setAll(catalog);
            orders.getItems().setAll(booked);
            tiles.getChildren().clear();
            for (Experience item : catalog) {
                tiles.getChildren().add(tile(item));
            }
            header.refreshPhoto();
            header.setHook(catalog.size() + " experiences  ·  " + booked.size() + " orders in this filter");
        });
    }

    private void addItem() {
        TextField name = new TextField();
        TextField category = new TextField();
        TextField price = new TextField();
        CheckBox active = new CheckBox("Active");
        active.setSelected(true);
        GridPane form = new GridPane();
        form.add(new Label("Name"), 0, 0);
        form.add(name, 1, 0);
        form.add(new Label("Category"), 0, 1);
        form.add(category, 1, 1);
        form.add(new Label("Price"), 0, 2);
        form.add(price, 1, 2);
        form.add(active, 1, 3);
        Experience[] draft = new Experience[1];
        Dialogs.form("Menu item", form, () -> {
            Experience item = new Experience();
            item.setName(name.getText());
            item.setCategory(category.getText());
            item.setPrice(Money.of(price.getText()));
            item.setActive(active.isSelected());
            draft[0] = item;
        });
        if (draft[0] != null) {
            Fx.run(() -> {
                experiences.saveItem(draft[0], true);
                return null;
            }, ignored -> refresh());
        }
    }

    private void place() {
        Fx.run(() -> new Object[] {experiences.menu(), new LodgeService().search(null, null, null, null)}, loaded -> {
            ComboBox<Experience> item = new ComboBox<>();
            item.getItems().setAll((java.util.List<Experience>) loaded[0]);
            ComboBox<String> type = new ComboBox<>();
            type.getItems().addAll("RESTAURANT", "ROOM_SERVICE");
            type.getSelectionModel().select(0);
            ComboBox<Lodge> lodge = new ComboBox<>();
            lodge.getItems().setAll((java.util.List<Lodge>) loaded[1]);
            TextField qty = new TextField("1");
            GridPane form = new GridPane();
            form.add(new Label("Item"), 0, 0);
            form.add(item, 1, 0);
            form.add(new Label("Type"), 0, 1);
            form.add(type, 1, 1);
            form.add(new Label("Lodge"), 0, 2);
            form.add(lodge, 1, 2);
            form.add(new Label("Qty"), 0, 3);
            form.add(qty, 1, 3);
            Object[] picked = new Object[4];
            Dialogs.form("Order", form, () -> {
                if (item.getValue() == null) {
                    throw new AppException("Choose a menu item.");
                }
                picked[0] = item.getValue();
                picked[1] = type.getValue();
                picked[2] = lodge.getValue();
                picked[3] = qty.getText();
            });
            if (picked[0] == null) {
                return;
            }
            Experience chosen = (Experience) picked[0];
            String orderType = (String) picked[1];
            Lodge room = (Lodge) picked[2];
            int count = Integer.parseInt(((String) picked[3]).trim());
            Long lodgeId = room == null ? null : room.getId();
            Fx.run(() -> {
                experiences.place(chosen.getId(), chosen.getName(), count, orderType, lodgeId);
                return null;
            }, ignored -> refresh());
        });
    }

    private void advance() {
        ExperienceBooking order = orders.getSelectionModel().getSelectedItem();
        if (order == null) {
            Fx.info("Select an order from the Orders table below first.");
            return;
        }
        ComboBox<String> next = new ComboBox<>();
        next.getItems().addAll(ORDER_STATUS);
        next.getSelectionModel().select(order.getStatus());
        GridPane form = new GridPane();
        form.add(new Label("Status"), 0, 0);
        form.add(next, 1, 0);
        String[] picked = new String[1];
        Dialogs.form("Order status", form, () -> picked[0] = next.getValue());
        if (picked[0] == null) {
            return;
        }
        Fx.run(() -> {
            experiences.advance(order.getId(), picked[0]);
            return null;
        }, ignored -> refresh());
    }

    private static StackPane tile(Experience item) {
        ImageView photo = new ImageView(ResortArt.forExperience(item.getCategory() + " " + item.getName()));
        photo.setPreserveRatio(true);
        photo.setSmooth(true);
        var img = photo.getImage();
        if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
            double scale = Math.max(200.0 / img.getWidth(), 118.0 / img.getHeight());
            photo.setFitWidth(img.getWidth() * scale);
            photo.setFitHeight(img.getHeight() * scale);
        }
        Region veil = new Region();
        veil.setStyle("-fx-background-color: linear-gradient(from 0% 45% to 0% 100%, transparent, rgba(20,26,34,0.9));");
        Label name = new Label(item.getName());
        name.setStyle("-fx-text-fill: #F7F4EF; -fx-font-weight: 700;");
        Label price = new Label(CurrencyUtils.format(item.getPrice()));
        price.setStyle("-fx-text-fill: #C4A35A;");
        VBox copy = new VBox(2, name, price);
        copy.setPadding(new Insets(8));
        copy.setAlignment(Pos.BOTTOM_LEFT);
        StackPane card = new StackPane(photo, veil, copy);
        card.getStyleClass().add("photo-card");
        card.setPrefWidth(200);
        card.setPrefHeight(118);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(200, 118);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        card.setClip(clip);
        StackPane.setAlignment(copy, Pos.BOTTOM_LEFT);
        return card;
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
