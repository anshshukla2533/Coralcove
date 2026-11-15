package coralcove.ui.stays;

import coralcove.model.BookingSource;
import coralcove.model.Guest;
import coralcove.model.Lodge;
import coralcove.model.StayBooking;
import coralcove.model.StayStatus;
import coralcove.service.GuestService;
import coralcove.service.LodgeService;
import coralcove.ui.Dialogs;
import coralcove.ui.Fx;
import coralcove.util.AppException;
import coralcove.util.DateUtils;
import coralcove.util.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public final class StayDialog {
    private static final String[] MEALS = {"EP", "CP", "MAP", "AP"};
    private static final String[] TRANSFERS = {"NONE", "AIRPORT", "JETTY"};

    private StayDialog() {
    }

    public static void show(StayBooking existing, Consumer<StayBooking> onSaved) {
        Fx.run(() -> new GuestService().search(""), guests -> open(existing, guests, onSaved));
    }

    private static void open(StayBooking existing, List<Guest> guests, Consumer<StayBooking> onSaved) {
        StayBooking[] result = new StayBooking[1];
        ComboBox<Guest> guest = new ComboBox<>();
        guest.getItems().setAll(guests);
        DatePicker checkIn = new DatePicker(existing == null ? DateUtils.today() : existing.getCheckIn());
        DatePicker checkOut = new DatePicker(existing == null ? DateUtils.today().plusDays(1) : existing.getCheckOut());
        Fx.dates(checkIn);
        Fx.dates(checkOut);
        ComboBox<Lodge> lodge = new ComboBox<>();
        Runnable loadLodges = () -> {
            LocalDate in = checkIn.getValue();
            LocalDate out = checkOut.getValue();
            if (in == null || out == null) {
                return;
            }
            Long ignore = existing == null ? null : existing.getId();
            Fx.run(() -> new LodgeService().availableForStay(in, out, ignore), rooms -> {
                lodge.getItems().setAll(rooms);
                if (existing != null) {
                    rooms.stream().filter(item -> item.getId() == existing.getLodgeId()).findFirst()
                            .ifPresent(lodge.getSelectionModel()::select);
                }
            });
        };
        checkIn.valueProperty().addListener((obs, a, b) -> loadLodges.run());
        checkOut.valueProperty().addListener((obs, a, b) -> loadLodges.run());
        TextField adults = new TextField(existing == null ? "1" : String.valueOf(existing.getAdults()));
        TextField children = new TextField(existing == null ? "0" : String.valueOf(existing.getChildren()));
        TextField discount = new TextField(existing == null ? "0" : existing.getDiscount().toPlainString());
        TextField requests = new TextField(existing == null ? "" : empty(existing.getSpecialRequests()));
        ComboBox<BookingSource> source = new ComboBox<>();
        source.getItems().setAll(BookingSource.values());
        source.getSelectionModel().select(existing == null ? BookingSource.WALK_IN : existing.getSource());
        ComboBox<String> meal = new ComboBox<>();
        meal.getItems().addAll(MEALS);
        meal.getSelectionModel().select(existing == null ? "EP" : existing.getMealPlan());
        ComboBox<String> transfer = new ComboBox<>();
        transfer.getItems().addAll(TRANSFERS);
        transfer.getSelectionModel().select(existing == null ? "NONE" : existing.getTransfer());
        if (existing != null) {
            guests.stream().filter(item -> item.getId() == existing.getGuestId()).findFirst()
                    .ifPresent(guest.getSelectionModel()::select);
        }
        GridPane form = new GridPane();
        int row = 0;
        form.add(new Label("Guest"), 0, row);
        form.add(guest, 1, row++);
        form.add(new Label("Check-in"), 0, row);
        form.add(checkIn, 1, row++);
        form.add(new Label("Check-out"), 0, row);
        form.add(checkOut, 1, row++);
        form.add(new Label("Lodge"), 0, row);
        form.add(lodge, 1, row++);
        form.add(new Label("Adults"), 0, row);
        form.add(adults, 1, row++);
        form.add(new Label("Children"), 0, row);
        form.add(children, 1, row++);
        form.add(new Label("Discount"), 0, row);
        form.add(discount, 1, row++);
        form.add(new Label("Source"), 0, row);
        form.add(source, 1, row++);
        form.add(new Label("Meal plan"), 0, row);
        form.add(meal, 1, row++);
        form.add(new Label("Transfer"), 0, row);
        form.add(transfer, 1, row++);
        form.add(new Label("Requests"), 0, row);
        form.add(requests, 1, row);
        loadLodges.run();
        Dialogs.form(existing == null ? "New stay" : "Edit stay", form, () -> {
            if (guest.getValue() == null || lodge.getValue() == null) {
                throw new AppException("Choose a guest and a lodge.");
            }
            StayBooking stay = existing == null ? new StayBooking() : existing;
            stay.setGuestId(guest.getValue().getId());
            stay.setLodgeId(lodge.getValue().getId());
            stay.setCheckIn(checkIn.getValue());
            stay.setCheckOut(checkOut.getValue());
            stay.setAdults(whole(adults.getText(), "Adults"));
            stay.setChildren(whole(children.getText(), "Children"));
            stay.setDiscount(Money.of(discount.getText().isBlank() ? "0" : discount.getText()));
            stay.setSource(source.getValue());
            stay.setMealPlan(meal.getValue());
            stay.setTransfer(transfer.getValue());
            stay.setSpecialRequests(requests.getText());
            if (existing == null) {
                stay.setStatus(StayStatus.CONFIRMED);
            }
            result[0] = stay;
        });
        if (result[0] != null) {
            onSaved.accept(result[0]);
        }
    }

    private static int whole(String raw, String field) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new AppException(field + " must be a whole number.");
        }
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }
}
