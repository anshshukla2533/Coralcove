package coralcove.ui.guests;

import coralcove.model.Guest;
import coralcove.service.GuestService;
import coralcove.ui.Columns;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ScreenHeader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class GuestView extends VBox implements Refreshable {
    private final GuestService guests = new GuestService();
    private final TextField search = new TextField();
    private final TableView<Guest> table = new TableView<>();
    private final ScreenHeader header = new ScreenHeader("Guests", "People staying with the group.");
    private final Label summaryName = new Label("Select a guest.");
    private final Label summaryMeta = new Label();
    private final Label summaryVip = new Label();
    private final VBox summaryAadhar = new VBox(6);

    public GuestView() {
        setSpacing(10);
        search.setPromptText("Name or phone");
        Button find = button("Search", false);
        find.setOnAction(event -> refresh());
        Button add = button("New guest", true);
        add.setOnAction(event -> edit(null));
        Button edit = button("Edit", false);
        edit.setOnAction(event -> {
            Guest guest = table.getSelectionModel().getSelectedItem();
            if (guest == null) {
                Fx.info("Select a guest from the table first.");
                return;
            }
            edit(guest);
        });
        Button remove = button("Delete", false);
        remove.setOnAction(event -> delete());
        table.getColumns().add(Columns.of("Name", Guest::getFullName));
        table.getColumns().add(Columns.of("Phone", Guest::getPhone));
        table.getColumns().add(Columns.of("Email", Guest::getEmail));
        table.getColumns().add(Columns.of("Nationality", Guest::getNationality));
        table.getColumns().add(Columns.of("Class", Guest::getVip));
        table.getColumns().add(Columns.of("Aadhaar", guest -> guest.isAadharVerified() ? "✓ Verified" : "Pending"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setMinHeight(320);
        summaryName.getStyleClass().add("panel-title");
        summaryMeta.getStyleClass().add("muted");
        summaryMeta.setWrapText(true);
        VBox summary = new VBox(10, summaryName, summaryVip, summaryMeta, summaryAadhar);
        summary.getStyleClass().add("guest-summary");
        table.getSelectionModel().selectedItemProperty().addListener((obs, a, guest) -> show(guest));
        HBox body = new HBox(12, table, summary);
        HBox.setHgrow(table, Priority.ALWAYS);
        VBox.setVgrow(body, Priority.ALWAYS);
        getChildren().addAll(header, Fx.bar(search, find, add, edit, remove), body);
    }

    @Override
    public void refresh() {
        String text = Fx.text(search);
        Fx.run(() -> guests.search(text), found -> {
            table.getItems().setAll(found);
            long vip = found.stream().filter(guest -> guest.getVip() != null && !"STANDARD".equals(guest.getVip())).count();
            header.refreshPhoto();
            header.setHook(found.size() + " guests  ·  " + vip + " on a higher class");
        });
    }

    private void show(Guest guest) {
        summaryAadhar.getChildren().clear();
        if (guest == null) {
            summaryName.setText("Select a guest.");
            summaryVip.setGraphic(null);
            summaryVip.setText("");
            summaryMeta.setText("");
            return;
        }
        summaryName.setText(guest.getFullName());
        summaryVip.setGraphic(Fx.chip(guest.getVip(), "VIP".equalsIgnoreCase(guest.getVip()) || "GOLD".equalsIgnoreCase(guest.getVip()) ? "vip" : "muted"));
        summaryVip.setText("");
        summaryMeta.setText((guest.getPhone() == null ? "" : guest.getPhone())
                + "\n" + (guest.getEmail() == null ? "" : guest.getEmail())
                + "\n" + (guest.getNationality() == null ? "" : guest.getNationality())
                + (guest.getIdDocNumber() != null && !guest.getIdDocNumber().isBlank() ? "\nID: " + guest.getIdDocNumber() : ""));

        Label aadharTitle = new Label("Aadhaar Verification");
        aadharTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B2430; -fx-padding: 8 0 2 0;");
        var aadharChip = Fx.chip(guest.isAadharVerified() ? "Aadhaar Verified" : "Aadhaar Pending",
                guest.isAadharVerified() ? "ready" : "held");

        var thumbnail = coralcove.ui.AadharUtils.createThumbnail(guest.getAadharImagePath(), 160, 100, () -> {
            var img = coralcove.ui.AadharUtils.loadAadharImage(guest.getAadharImagePath());
            if (img != null) {
                coralcove.ui.AadharUtils.showViewer(coralcove.Main.stage(), "Aadhaar Card · " + guest.getFullName(), img);
            }
        });

        summaryAadhar.getChildren().addAll(aadharTitle, aadharChip, thumbnail);
    }

    private void edit(Guest existing) {
        Guest draft = GuestDialog.show(existing);
        if (draft == null) {
            return;
        }
        boolean create = existing == null;
        Fx.run(() -> guests.save(draft, create), saved -> refresh());
    }

    private void delete() {
        Guest guest = table.getSelectionModel().getSelectedItem();
        if (guest == null) {
            Fx.info("Select a guest from the table first.");
            return;
        }
        if (!Fx.confirm("Delete " + guest.getFullName() + "?")) {
            return;
        }
        Fx.run(() -> {
            guests.delete(guest.getId());
            return null;
        }, ignored -> refresh());
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
