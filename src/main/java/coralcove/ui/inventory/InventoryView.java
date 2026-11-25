package coralcove.ui.inventory;

import coralcove.model.InventoryItem;
import coralcove.service.InventoryService;
import coralcove.ui.Columns;
import coralcove.ui.Dialogs;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ScreenHeader;
import javafx.scene.layout.HBox;
import coralcove.util.AppException;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class InventoryView extends VBox implements Refreshable {
    private final InventoryService stock = new InventoryService();
    private final TextField search = new TextField();
    private final ComboBox<String> category = new ComboBox<>();
    private final TableView<InventoryItem> items = new TableView<>();
    private final TableView<String[]> history = Columns.strings("When", "Reason", "Change", "Note");
    private final ScreenHeader header = new ScreenHeader("Inventory", "Stock, suppliers, and movements.");
    private final HBox banner = new HBox(8);

    public InventoryView() {
        setSpacing(10);
        search.setPromptText("SKU or name");
        category.getItems().add("");
        category.getItems().addAll(InventoryService.CATEGORIES);
        category.getSelectionModel().select(0);
        items.getColumns().add(Columns.of("SKU", InventoryItem::getSku));
        items.getColumns().add(Columns.of("Item", InventoryItem::getName));
        items.getColumns().add(Columns.of("Category", InventoryItem::getCategory));
        items.getColumns().add(Columns.of("Qty", item -> String.valueOf(item.getQuantity())));
        items.getColumns().add(Columns.of("Minimum", item -> String.valueOf(item.getMinQuantity())));
        items.getColumns().add(Columns.of("Unit", InventoryItem::getUnit));
        items.getColumns().add(Columns.of("Supplier", InventoryItem::getSupplierName));
        items.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        items.setRowFactory(view -> new TableRow<>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("low-stock");
                if (!empty && item != null && item.isLow()) {
                    getStyleClass().add("low-stock");
                }
            }
        });
        items.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> loadHistory());
        Button find = button("Search", false);
        find.setOnAction(event -> refresh());
        Button add = button("New item", true);
        add.setOnAction(event -> edit(null));
        Button edit = button("Edit", false);
        edit.setOnAction(event -> {
            InventoryItem item = items.getSelectionModel().getSelectedItem();
            if (item != null) {
                edit(item);
            }
        });
        Button adjust = button("Adjust stock", false);
        adjust.setOnAction(event -> adjust());
        Button supplier = button("Add supplier", false);
        supplier.setOnAction(event -> addSupplier());
        VBox.setVgrow(items, Priority.ALWAYS);
        items.setMinHeight(220);
        items.setPrefHeight(260);
        history.setMinHeight(170);
        history.setPrefHeight(190);
        banner.getStyleClass().add("danger-banner");
        banner.setVisible(false);
        banner.setManaged(false);
        getChildren().addAll(header, banner, Fx.bar(search, category, find, add, edit, adjust, supplier), items,
                new Label("Movements"), history);
    }

    @Override
    public void refresh() {
        String text = Fx.text(search);
        String group = category.getValue();
        Fx.run(() -> stock.search(text, group), found -> {
            items.getItems().setAll(found);
            int low = 0;
            for (InventoryItem item : found) {
                if (item.isLow()) {
                    low++;
                }
            }
            banner.getChildren().setAll(new Label(low + " items below minimum. Reorder before evening service."));
            banner.setVisible(low > 0);
            banner.setManaged(low > 0);
            header.refreshPhoto();
            header.setHook(found.size() + " SKUs  ·  " + low + " below the floor");
        });
    }

    private void loadHistory() {
        InventoryItem item = items.getSelectionModel().getSelectedItem();
        if (item == null) {
            Columns.fill(history, java.util.List.of());
            return;
        }
        Fx.run(() -> stock.history(item.getId()), rows -> Columns.fill(history, rows));
    }

    private void edit(InventoryItem existing) {
        Fx.run(stock::suppliers, suppliers -> {
            TextField sku = new TextField(existing == null ? "" : existing.getSku());
            TextField name = new TextField(existing == null ? "" : existing.getName());
            ComboBox<String> group = new ComboBox<>();
            group.getItems().addAll(InventoryService.CATEGORIES);
            group.getSelectionModel().select(existing == null ? InventoryService.CATEGORIES.get(0) : existing.getCategory());
            ComboBox<String> supplier = new ComboBox<>();
            supplier.getItems().add("");
            for (String[] row : suppliers) {
                supplier.getItems().add(row[0] + " " + row[1]);
            }
            TextField qty = new TextField(existing == null ? "0" : String.valueOf(existing.getQuantity()));
            TextField min = new TextField(existing == null ? "0" : String.valueOf(existing.getMinQuantity()));
            TextField unit = new TextField(existing == null ? "pcs" : existing.getUnit());
            GridPane form = new GridPane();
            form.add(new Label("SKU"), 0, 0);
            form.add(sku, 1, 0);
            form.add(new Label("Name"), 0, 1);
            form.add(name, 1, 1);
            form.add(new Label("Category"), 0, 2);
            form.add(group, 1, 2);
            form.add(new Label("Supplier"), 0, 3);
            form.add(supplier, 1, 3);
            form.add(new Label("Quantity"), 0, 4);
            form.add(qty, 1, 4);
            form.add(new Label("Minimum"), 0, 5);
            form.add(min, 1, 5);
            form.add(new Label("Unit"), 0, 6);
            form.add(unit, 1, 6);
            InventoryItem[] draft = new InventoryItem[1];
            Dialogs.form(existing == null ? "Stock item" : "Edit item", form, () -> {
                InventoryItem item = existing == null ? new InventoryItem() : existing;
                item.setSku(sku.getText());
                item.setName(name.getText());
                item.setCategory(group.getValue());
                item.setQuantity(Integer.parseInt(qty.getText().trim()));
                item.setMinQuantity(Integer.parseInt(min.getText().trim()));
                item.setUnit(unit.getText());
                String chosen = supplier.getValue();
                if (chosen == null || chosen.isBlank()) {
                    item.setSupplierId(null);
                } else {
                    item.setSupplierId(Long.parseLong(chosen.substring(0, chosen.indexOf(' '))));
                }
                draft[0] = item;
            });
            if (draft[0] != null) {
                boolean create = existing == null;
                Fx.run(() -> stock.save(draft[0], create), saved -> refresh());
            }
        });
    }

    private void adjust() {
        InventoryItem item = items.getSelectionModel().getSelectedItem();
        if (item == null) {
            Fx.error(new AppException("Select a stock item first."));
            return;
        }
        TextField delta = new TextField();
        TextField reason = new TextField();
        TextField note = new TextField();
        GridPane form = new GridPane();
        form.add(new Label("Change"), 0, 0);
        form.add(delta, 1, 0);
        form.add(new Label("Reason"), 0, 1);
        form.add(reason, 1, 1);
        form.add(new Label("Note"), 0, 2);
        form.add(note, 1, 2);
        String[] picked = new String[1];
        Dialogs.form("Adjust " + item.getName(), form, () -> picked[0] = delta.getText().trim() + "|" + reason.getText() + "|" + note.getText());
        if (picked[0] == null) {
            return;
        }
        String[] parts = picked[0].split("\\|", 3);
        Fx.run(() -> {
            stock.adjust(item.getId(), Integer.parseInt(parts[0]), parts[1], parts.length > 2 ? parts[2] : "");
            return null;
        }, ignored -> refresh());
    }

    private void addSupplier() {
        TextField name = new TextField();
        TextField phone = new TextField();
        TextField email = new TextField();
        GridPane form = new GridPane();
        form.add(new Label("Name"), 0, 0);
        form.add(name, 1, 0);
        form.add(new Label("Phone"), 0, 1);
        form.add(phone, 1, 1);
        form.add(new Label("Email"), 0, 2);
        form.add(email, 1, 2);
        String[] picked = new String[1];
        Dialogs.form("Supplier", form, () -> picked[0] = name.getText() + "|" + phone.getText() + "|" + email.getText());
        if (picked[0] == null) {
            return;
        }
        String[] parts = picked[0].split("\\|", 3);
        Fx.run(() -> stock.addSupplier(parts[0], parts[1], parts.length > 2 ? parts[2] : ""), id -> refresh());
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
