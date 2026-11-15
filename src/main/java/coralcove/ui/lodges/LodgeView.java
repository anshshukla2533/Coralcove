package coralcove.ui.lodges;

import coralcove.model.Lodge;
import coralcove.model.LodgeStatus;
import coralcove.model.Role;
import coralcove.service.LodgeService;
import coralcove.service.ResortService;
import coralcove.service.Session;
import coralcove.ui.Columns;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ResortArt;
import coralcove.ui.ScreenHeader;
import coralcove.util.CurrencyUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class LodgeView extends VBox implements Refreshable {
    private final LodgeService lodges = new LodgeService();
    private final TextField search = new TextField();
    private final TableView<Lodge> table = new TableView<>();
    private final FlowPane cards = new FlowPane(10, 10);
    private ListHolder rows = new ListHolder();
    private final ScreenHeader header = new ScreenHeader("Lodges", "Units by zone and status.");

    public LodgeView() {
        setSpacing(10);
        search.setPromptText("Search unit code");
        Button find = secondary("Search");
        find.setOnAction(event -> refresh());
        Button add = primary("New lodge");
        add.setOnAction(event -> edit(null));
        Button edit = secondary("Edit");
        edit.setOnAction(event -> {
            Lodge selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                edit(selected);
            }
        });
        Button status = secondary("Status");
        status.setOnAction(event -> changeStatus());
        Button delete = secondary("Delete");
        delete.setOnAction(event -> delete());
        boolean write = canWrite();
        add.setDisable(!write);
        edit.setDisable(!write);
        status.setDisable(!write);
        delete.setDisable(!write);
        table.getColumns().add(Columns.of("Resort", Lodge::getResortName));
        table.getColumns().add(Columns.of("Unit", Lodge::getUnitCode));
        table.getColumns().add(Columns.of("Zone", Lodge::getZone));
        table.getColumns().add(Columns.of("View", Lodge::getViewAspect));
        table.getColumns().add(Columns.of("Category", Lodge::getRoomTypeName));
        table.getColumns().add(Columns.of("Guests", lodge -> String.valueOf(lodge.getMaxGuests())));
        table.getColumns().add(Columns.of("Price", lodge -> CurrencyUtils.format(lodge.getPricePerNight())));
        table.getColumns().add(Columns.of("Status", lodge -> lodge.getStatus().name()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);
        ScrollPane scroller = new ScrollPane(cards);
        scroller.setFitToWidth(true);
        scroller.setPrefHeight(180);
        cards.setPadding(new Insets(4));
        getChildren().addAll(header, Fx.bar(search, find, add, edit, status, delete), table, scroller);
    }

    @Override
    public void refresh() {
        String text = Fx.text(search);
        Fx.run(() -> lodges.search(text, null, null, null), found -> {
            rows.items = found;
            table.getItems().setAll(found);
            cards.getChildren().clear();
            for (Lodge lodge : found) {
                cards.getChildren().add(card(lodge));
            }
            header.refreshPhoto();
            header.setHook(found.size() + " lodges  ·  photos follow beach, lagoon, garden, and cliff");
        });
    }

    private void edit(Lodge existing) {
        Fx.run(() -> new Object[] {lodges.types(), new ResortService().list(true)}, loaded -> {
            var types = (java.util.List<coralcove.model.LodgeCategory>) loaded[0];
            var resorts = (java.util.List<coralcove.model.Resort>) loaded[1];
            Lodge draft = LodgeDialog.show(existing, types, resorts);
            if (draft == null) {
                return;
            }
            boolean create = existing == null;
            Fx.run(() -> lodges.save(draft, create), saved -> refresh());
        });
    }

    private void changeStatus() {
        Lodge lodge = table.getSelectionModel().getSelectedItem();
        if (lodge == null) {
            return;
        }
        ComboBox<LodgeStatus> choice = new ComboBox<>();
        for (LodgeStatus status : LodgeStatus.values()) {
            if (status != LodgeStatus.IN_STAY) {
                choice.getItems().add(status);
            }
        }
        choice.getSelectionModel().select(lodge.getStatus() == LodgeStatus.IN_STAY ? LodgeStatus.HELD : lodge.getStatus());
        javafx.scene.layout.GridPane form = new javafx.scene.layout.GridPane();
        form.add(new Label("New status"), 0, 0);
        form.add(choice, 1, 0);
        LodgeStatus[] picked = new LodgeStatus[1];
        coralcove.ui.Dialogs.form("Lodge status", form, () -> {
            if (choice.getValue() == null) {
                throw new coralcove.util.AppException("Choose a status.");
            }
            picked[0] = choice.getValue();
        });
        if (picked[0] != null) {
            Fx.run(() -> {
                lodges.changeStatus(lodge.getId(), picked[0]);
                return null;
            }, ignored -> refresh());
        }
    }

    private void delete() {
        Lodge lodge = table.getSelectionModel().getSelectedItem();
        if (lodge == null) {
            return;
        }
        if (!Fx.confirm("Delete lodge " + lodge.getUnitCode() + "?")) {
            return;
        }
        Fx.run(() -> {
            lodges.delete(lodge.getId());
            return null;
        }, ignored -> refresh());
    }

    private StackPane card(Lodge lodge) {
        ImageView photo = new ImageView(ResortArt.forZone(lodge.getZone()));
        photo.setPreserveRatio(true);
        photo.setSmooth(true);
        var img = photo.getImage();
        if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
            double scale = Math.max(180.0 / img.getWidth(), 112.0 / img.getHeight());
            photo.setFitWidth(img.getWidth() * scale);
            photo.setFitHeight(img.getHeight() * scale);
        }
        Region veil = new Region();
        veil.setStyle("-fx-background-color: linear-gradient(from 0% 40% to 0% 100%, transparent, rgba(20,26,34,0.88));");
        Label code = new Label(lodge.getUnitCode());
        code.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        Label meta = new Label(lodge.getRoomTypeName() + " · " + lodge.getStatus().label());
        meta.setStyle("-fx-text-fill: #E4DDD2;");
        VBox copy = new VBox(2, code, meta);
        copy.setPadding(new Insets(10));
        copy.setAlignment(Pos.BOTTOM_LEFT);
        Region bar = new Region();
        bar.setPrefHeight(5);
        bar.setMaxHeight(5);
        bar.setStyle("-fx-background-color: " + color(lodge.getStatus()) + "; -fx-background-radius: 8 8 0 0;");
        StackPane card = new StackPane(photo, veil, copy, bar);
        StackPane.setAlignment(bar, Pos.TOP_CENTER);
        StackPane.setAlignment(copy, Pos.BOTTOM_LEFT);
        card.getStyleClass().add("photo-card");
        card.setPrefWidth(180);
        card.setPrefHeight(112);
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(180, 112);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        card.setClip(clip);
        return card;
    }

    private static String color(LodgeStatus status) {
        return switch (status) {
            case READY -> "#1F7A4D";
            case IN_STAY -> "#9B2335";
            case HELD -> "#C47B17";
            case TURNDOWN -> "#1D4E89";
            case REPAIR -> "#6B7280";
            case CLOSED -> "#374151";
        };
    }

    private static boolean canWrite() {
        Role role = Session.get() == null ? null : Session.get().getRole();
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    private static Button primary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary");
        return button;
    }

    private static Button secondary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary");
        return button;
    }

    private static final class ListHolder {
        java.util.List<Lodge> items = java.util.List.of();
    }
}
