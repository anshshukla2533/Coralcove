package coralcove.ui.lodges;

import coralcove.model.Lodge;
import coralcove.model.LodgeCategory;
import coralcove.model.LodgeStatus;
import coralcove.model.Resort;
import coralcove.service.Session;
import coralcove.ui.Dialogs;
import coralcove.util.AppException;
import coralcove.util.Money;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public final class LodgeDialog {
    private static final String[] ZONES = {"BEACH", "LAGOON", "GARDEN", "CLIFF"};
    private static final String[] VIEWS = {"SEA", "GARDEN", "POOL"};

    private LodgeDialog() {
    }

    public static Lodge show(Lodge existing, List<LodgeCategory> types, List<Resort> resorts) {
        Lodge[] result = new Lodge[1];
        ComboBox<Resort> resort = new ComboBox<>();
        resort.getItems().setAll(resorts);
        ComboBox<String> zone = new ComboBox<>();
        zone.getItems().addAll(ZONES);
        zone.getSelectionModel().select(existing == null ? "BEACH" : existing.getZone());
        ComboBox<String> view = new ComboBox<>();
        view.getItems().addAll(VIEWS);
        view.getSelectionModel().select(existing == null ? "SEA" : existing.getViewAspect());
        ComboBox<LodgeCategory> type = new ComboBox<>();
        type.getItems().setAll(types);
        if (!types.isEmpty()) {
            type.getSelectionModel().select(0);
        }
        TextField number = new TextField(existing == null ? "" : existing.getUnitCode());
        TextField beds = new TextField(existing == null ? "1 King" : existing.getBedConfig());
        TextField guests = new TextField(existing == null ? "2" : String.valueOf(existing.getMaxGuests()));
        TextField price = new TextField(existing == null ? "" : existing.getPricePerNight().toPlainString());
        TextField amenities = new TextField(existing == null ? "" : empty(existing.getAmenities()));
        TextArea description = new TextArea(existing == null ? "" : empty(existing.getDescription()));
        description.setPrefRowCount(3);
        ComboBox<LodgeStatus> status = new ComboBox<>();
        status.getItems().setAll(LodgeStatus.values());
        status.getSelectionModel().select(existing == null ? LodgeStatus.READY : existing.getStatus());
        if (existing != null) {
            types.stream().filter(item -> item.getId() == existing.getLodgeCategoryId()).findFirst().ifPresent(type.getSelectionModel()::select);
            resorts.stream().filter(item -> item.getId() == existing.getResortId()).findFirst().ifPresent(resort.getSelectionModel()::select);
        } else if (Session.resortFilter() != null) {
            resorts.stream().filter(item -> item.getId() == Session.resortFilter()).findFirst().ifPresent(resort.getSelectionModel()::select);
        }
        boolean pickResort = Session.resortFilter() == null;
        GridPane form = new GridPane();
        int row = 0;
        if (pickResort) {
            form.add(new javafx.scene.control.Label("Resort"), 0, row);
            form.add(resort, 1, row++);
        }
        form.add(new javafx.scene.control.Label("Unit code"), 0, row);
        form.add(number, 1, row++);
        form.add(new javafx.scene.control.Label("Zone"), 0, row);
        form.add(zone, 1, row++);
        form.add(new javafx.scene.control.Label("View"), 0, row);
        form.add(view, 1, row++);
        form.add(new javafx.scene.control.Label("Category"), 0, row);
        form.add(type, 1, row++);
        form.add(new javafx.scene.control.Label("Beds"), 0, row);
        form.add(beds, 1, row++);
        form.add(new javafx.scene.control.Label("Max guests"), 0, row);
        form.add(guests, 1, row++);
        form.add(new javafx.scene.control.Label("Price / night (INR)"), 0, row);
        form.add(price, 1, row++);
        form.add(new javafx.scene.control.Label("Amenities"), 0, row);
        form.add(amenities, 1, row++);
        form.add(new javafx.scene.control.Label("Description"), 0, row);
        form.add(description, 1, row++);
        form.add(new javafx.scene.control.Label("Status"), 0, row);
        form.add(status, 1, row);
        Dialogs.form(existing == null ? "Add lodge" : "Edit lodge", form, () -> {
            if (resort.getValue() == null) {
                throw new AppException("Choose a resort before saving.");
            }
            if (type.getValue() == null) {
                throw new AppException("Choose a category.");
            }
            Lodge room = existing == null ? new Lodge() : existing;
            String code = number.getText();
            String bed = beds.getText();
            int max;
            try {
                max = Integer.parseInt(guests.getText().trim());
            } catch (NumberFormatException ex) {
                throw new AppException("Max guests must be a whole number.");
            }
            var nightly = Money.of(price.getText());
            room.setResortId(resort.getValue().getId());
            room.setUnitCode(code);
            room.setZone(zone.getValue());
            room.setViewAspect(view.getValue());
            room.setLodgeCategoryId(type.getValue().getId());
            room.setBedConfig(bed);
            room.setMaxGuests(max);
            room.setPricePerNight(nightly);
            room.setAmenities(amenities.getText());
            room.setDescription(description.getText());
            room.setStatus(status.getValue());
            result[0] = room;
        });
        return result[0];
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }
}
