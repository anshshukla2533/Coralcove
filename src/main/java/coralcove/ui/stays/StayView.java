package coralcove.ui.stays;

import coralcove.model.StayBooking;
import coralcove.model.StayStatus;
import coralcove.service.StayService;
import coralcove.ui.Columns;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ScreenHeader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import coralcove.util.CurrencyUtils;
import coralcove.util.DateUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class StayView extends VBox implements Refreshable {
    private final StayService stays = new StayService();
    private final TextField reference = new TextField();
    private final TextField guest = new TextField();
    private final DatePicker from = new DatePicker();
    private final DatePicker to = new DatePicker();
    private final ComboBox<String> status = new ComboBox<>();
    private final TableView<StayBooking> table = new TableView<>();
    private final ScreenHeader header = new ScreenHeader("Stays", "Bookings for the selected resort.");

    public StayView() {
        setSpacing(10);
        reference.setPromptText("Reference");
        guest.setPromptText("Guest");
        Fx.dates(from);
        Fx.dates(to);
        status.getItems().add("");
        for (StayStatus value : StayStatus.values()) {
            status.getItems().add(value.name());
        }
        status.getSelectionModel().select(0);
        Button find = button("Search", false);
        find.setOnAction(event -> refresh());
        Button add = button("New stay", true);
        add.setOnAction(event -> StayDialog.show(null, draft ->
                Fx.run(() -> stays.create(draft), saved -> refresh())));
        Button edit = button("Edit", false);
        edit.setOnAction(event -> {
            StayBooking stay = table.getSelectionModel().getSelectedItem();
            if (stay == null) {
                Fx.info("Select a stay from the table first.");
                return;
            }
            StayDialog.show(stay, draft -> Fx.run(() -> stays.update(draft), saved -> refresh()));
        });
        Button cancel = button("Cancel stay", false);
        cancel.setOnAction(event -> cancel());
        table.getColumns().add(Columns.of("Reference", StayBooking::getBookingReference));
        table.getColumns().add(Columns.of("Guest", StayBooking::getGuestName));
        table.getColumns().add(Columns.of("Lodge", StayBooking::getUnitCode));
        table.getColumns().add(Columns.of("Check-in", stay -> DateUtils.display(stay.getCheckIn())));
        table.getColumns().add(Columns.of("Check-out", stay -> DateUtils.display(stay.getCheckOut())));
        TableColumn<StayBooking, String> statusCol = Columns.of("Status", stay -> stay.getStatus().name());
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setGraphic(empty || value == null ? null : Fx.chip(value, value));
                setText(null);
            }
        });
        table.getColumns().add(statusCol);
        table.getColumns().add(Columns.of("Total", stay -> CurrencyUtils.format(stay.getTotal())));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setMinHeight(320);
        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(header, Fx.bar(reference, guest, from, to, status, find, add, edit, cancel), table);
    }

    @Override
    public void refresh() {
        String ref = Fx.text(reference);
        String who = Fx.text(guest);
        var start = from.getValue();
        var end = to.getValue();
        String state = status.getValue();
        Fx.run(() -> stays.search(ref, who, start, end, state), found -> {
            table.getItems().setAll(found);
            var today = DateUtils.today();
            long in = found.stream().filter(stay -> today.equals(stay.getCheckIn())).count();
            long out = found.stream().filter(stay -> today.equals(stay.getCheckOut())).count();
            header.refreshPhoto();
            header.setHook(in + " check-ins today  ·  " + out + " check-outs today  ·  " + found.size() + " stays in this list");
        });
    }

    private void cancel() {
        StayBooking stay = table.getSelectionModel().getSelectedItem();
        if (stay == null) {
            Fx.info("Select a stay from the table first.");
            return;
        }
        if (!Fx.confirm("Cancel " + stay.getBookingReference() + "?")) {
            return;
        }
        Fx.run(() -> {
            stays.cancel(stay.getId());
            return null;
        }, ignored -> refresh());
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
