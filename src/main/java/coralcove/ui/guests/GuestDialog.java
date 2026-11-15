package coralcove.ui.guests;

import coralcove.Main;
import coralcove.model.Guest;
import coralcove.ui.AadharUtils;
import coralcove.ui.Dialogs;
import coralcove.ui.Fx;
import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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
        TextField idType = field(existing == null ? "Aadhaar" : existing.getIdDocType());
        TextField idNumber = field(existing == null ? "" : existing.getIdDocNumber());
        TextField preferences = field(existing == null ? "" : existing.getPreferences());

        CheckBox aadharVerified = new CheckBox("Aadhaar Card Verified");
        aadharVerified.setSelected(existing != null && existing.isAadharVerified());

        final String[] aadharPath = {existing == null ? null : existing.getAadharImagePath()};
        StackPane thumbnailPane = new StackPane();

        Runnable updateThumb = () -> {
            thumbnailPane.getChildren().clear();
            var thumb = AadharUtils.createThumbnail(aadharPath[0], 150, 95, () -> {
                var img = AadharUtils.loadAadharImage(aadharPath[0]);
                if (img != null) {
                    AadharUtils.showViewer(Main.stage(), "Aadhaar Card · " + name.getText(), img);
                }
            });
            thumbnailPane.getChildren().add(thumb);
        };
        updateThumb.run();

        Button uploadAadharBtn = new Button("Upload Aadhaar Photo");
        uploadAadharBtn.getStyleClass().add("secondary");
        uploadAadharBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose Aadhaar Card Photo");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg, *.webp)", "*.png", "*.jpg", "*.jpeg", "*.webp")
            );
            File file = chooser.showOpenDialog(Main.stage());
            if (file != null) {
                try {
                    long gid = existing == null ? System.currentTimeMillis() : existing.getId();
                    String saved = AadharUtils.saveUploadedAadhar(gid, file);
                    aadharPath[0] = saved;
                    aadharVerified.setSelected(true);
                    updateThumb.run();
                } catch (Exception ex) {
                    Fx.error(ex);
                }
            }
        });

        Button sampleAadharBtn = new Button("Sample card");
        sampleAadharBtn.getStyleClass().add("secondary");
        sampleAadharBtn.setOnAction(e -> {
            aadharPath[0] = "sample-aadhar.png";
            aadharVerified.setSelected(true);
            if (idNumber.getText().isBlank()) {
                idNumber.setText("XXXX-XXXX-9012");
            }
            updateThumb.run();
        });

        Button clearAadharBtn = new Button("Clear");
        clearAadharBtn.getStyleClass().add("secondary");
        clearAadharBtn.setOnAction(e -> {
            aadharPath[0] = null;
            updateThumb.run();
        });

        HBox aadharButtons = new HBox(8, uploadAadharBtn, sampleAadharBtn, clearAadharBtn);
        aadharButtons.setAlignment(Pos.CENTER_LEFT);

        VBox aadharUploadBox = new VBox(6, thumbnailPane, aadharButtons);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        int row = 0;
        row = add(form, row, "Full name", name);
        row = add(form, row, "Phone", phone);
        row = add(form, row, "Email", email);
        row = add(form, row, "Gender", gender);
        row = add(form, row, "Nationality", nationality);
        row = add(form, row, "Address", address);
        row = add(form, row, "ID doc type", idType);
        row = add(form, row, "Aadhaar / ID number", idNumber);
        form.add(new Label("Aadhaar verification"), 0, row);
        form.add(aadharVerified, 1, row++);
        form.add(new Label("Aadhaar card image"), 0, row);
        form.add(aadharUploadBox, 1, row++);
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
            guest.setAadharVerified(aadharVerified.isSelected());
            guest.setAadharImagePath(aadharPath[0]);
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
