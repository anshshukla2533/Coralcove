package coralcove.ui.guests;

import coralcove.model.Guest;
import coralcove.ui.Dialogs;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public final class GuestDialog {
    private GuestDialog() {
    }

    public static Guest show(Guest existing) {
        Guest[] result = new Guest[1];
        TextField name = field(existing == null ? "" : existing.getFullName());
        TextField phone = field(existing == null ? "" : existing.getPhone());
        TextField email = field(existing == null ? "" : existing.getEmail());
        TextField gender = field(existing == null ? "" : existing.getGender());
        TextField nationality = field(existing == null ? "India" : existing.getNationality());
        TextField address = field(existing == null ? "" : existing.getAddress());
        TextField idType = field(existing == null ? "" : existing.getIdDocType());
        TextField idNumber = field(existing == null ? "" : existing.getIdDocNumber());
        TextField preferences = field(existing == null ? "" : existing.getPreferences());
        GridPane form = new GridPane();
        int row = 0;
        row = add(form, row, "Full name", name);
        row = add(form, row, "Phone", phone);
        row = add(form, row, "Email", email);
        row = add(form, row, "Gender", gender);
        row = add(form, row, "Nationality", nationality);
        row = add(form, row, "Address", address);
        row = add(form, row, "ID type", idType);
        row = add(form, row, "ID number", idNumber);
        add(form, row, "Preferences", preferences);
        Dialogs.form(existing == null ? "Add guest" : "Edit guest", form, () -> {
            Guest guest = existing == null ? new Guest() : existing;
            guest.setFullName(name.getText());
            guest.setPhone(phone.getText());
            guest.setEmail(email.getText());
            guest.setGender(gender.getText());
            guest.setNationality(nationality.getText());
            guest.setAddress(address.getText());
            guest.setIdDocType(idType.getText());
            guest.setIdDocNumber(idNumber.getText());
            guest.setPreferences(preferences.getText());
            result[0] = guest;
        });
        return result[0];
    }

    private static TextField field(String value) {
        return new TextField(value == null ? "" : value);
    }

    private static int add(GridPane form, int row, String label, TextField field) {
        form.add(new Label(label), 0, row);
        form.add(field, 1, row);
        return row + 1;
    }
}
