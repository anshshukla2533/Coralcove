package coralcove.ui.arrivals;

import coralcove.Main;
import coralcove.model.Guest;
import coralcove.model.Lodge;
import coralcove.model.StayBooking;
import coralcove.model.StayStatus;
import coralcove.service.ArrivalService;
import coralcove.service.GuestService;
import coralcove.service.LodgeService;
import coralcove.service.StayService;
import coralcove.ui.AadharUtils;
import coralcove.ui.Columns;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ResortArt;
import coralcove.ui.ScreenHeader;
import coralcove.util.CurrencyUtils;
import coralcove.util.DateUtils;
import java.io.File;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public final class ArrivalView extends VBox implements Refreshable {
    private final ArrivalService arrivals = new ArrivalService();
    private final GuestService guestService = new GuestService();
    private final LodgeService lodgeService = new LodgeService();
    private final StayService stayService = new StayService();

    private final TextField reference = new TextField();
    private final ScreenHeader header = new ScreenHeader("Arrivals", "Walk-in registration, check-in, and guest Aadhaar identity verification.");
    private final ImageView photo = new ImageView(ResortArt.image("zone-lagoon"));
    private final Label guestTitle = new Label("Enter a booking reference or select a guest from the list below.");
    private final Label meta = new Label();
    private final VBox aadharContainer = new VBox(8);
    private final TableView<StayBooking> arrivalTable = new TableView<>();

    private StayBooking current;
    private Guest currentGuest;

    public ArrivalView() {
        setSpacing(12);
        reference.setPromptText("Enter booking reference");

        Button find = button("Find", false);
        find.setOnAction(event -> lookup());

        Button in = button("Check in", true);
        in.setOnAction(event -> act(true));

        Button out = button("Check out", false);
        out.setOnAction(event -> act(false));

        photo.setFitWidth(280);
        photo.setFitHeight(180);
        photo.setPreserveRatio(false);

        guestTitle.getStyleClass().add("panel-title");
        guestTitle.setWrapText(true);

        meta.getStyleClass().add("muted");
        meta.setWrapText(true);

        VBox copy = new VBox(8, guestTitle, meta, aadharContainer);
        copy.setPadding(new Insets(16));
        HBox.setHgrow(copy, Priority.ALWAYS);

        HBox card = new HBox(0, photo, copy);
        card.getStyleClass().add("stay-card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Arrival / In-house Quick Selection Table
        arrivalTable.getColumns().add(Columns.of("Reference", StayBooking::getBookingReference));
        arrivalTable.getColumns().add(Columns.of("Guest", StayBooking::getGuestName));
        arrivalTable.getColumns().add(Columns.of("Unit", StayBooking::getUnitCode));
        arrivalTable.getColumns().add(Columns.of("Check-in", s -> DateUtils.display(s.getCheckIn())));
        arrivalTable.getColumns().add(Columns.of("Check-out", s -> DateUtils.display(s.getCheckOut())));
        arrivalTable.getColumns().add(Columns.of("Status", s -> s.getStatus().name()));
        arrivalTable.getColumns().add(Columns.of("Aadhaar", s -> {
            Guest g = guestService.findById(s.getGuestId());
            return (g != null && g.isAadharVerified()) ? "✓ Verified" : "Pending";
        }));
        arrivalTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        arrivalTable.setPrefHeight(220);
        arrivalTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                reference.setText(selected.getBookingReference());
                show(selected);
            }
        });

        Label tableHeading = new Label("Expected & Active Arrivals for Front Desk");
        tableHeading.getStyleClass().add("section-title");
        tableHeading.setStyle("-fx-font-size: 16px; -fx-padding: 6 0 2 0;");

        getChildren().addAll(header, Fx.bar(reference, find, in, out), card, tableHeading, arrivalTable);
    }

    @Override
    public void refresh() {
        header.refreshPhoto();
        Fx.run(() -> stayService.search(null, null, null, null, null), list -> {
            arrivalTable.getItems().setAll(list);
            if (!Fx.text(reference).isBlank()) {
                lookup();
            } else if (!list.isEmpty() && current == null) {
                StayBooking pick = list.stream()
                        .filter(s -> s.getStatus() == StayStatus.CONFIRMED || s.getStatus() == StayStatus.ARRIVED)
                        .findFirst()
                        .orElse(list.get(0));
                reference.setText(pick.getBookingReference());
                show(pick);
            }
        });
    }

    private void lookup() {
        String ref = Fx.text(reference);
        if (ref.isBlank()) {
            return;
        }
        Fx.run(() -> arrivals.findByReference(ref), this::show, Fx::error);
    }

    private void act(boolean checkIn) {
        if (current == null) {
            guestTitle.setText("Find a stay first.");
            meta.setText("");
            return;
        }
        long id = current.getId();
        Fx.run(() -> checkIn ? arrivals.checkIn(id) : arrivals.checkOut(id), this::show);
    }

    private void show(StayBooking stay) {
        current = stay;
        aadharContainer.getChildren().clear();

        if (stay == null) {
            guestTitle.setText("No stay matches that reference.");
            meta.setText("");
            photo.setImage(ResortArt.image("zone-lagoon"));
            return;
        }

        currentGuest = guestService.findById(stay.getGuestId());
        Lodge lodge = lodgeService.findById(stay.getLodgeId());

        if (lodge != null) {
            photo.setImage(ResortArt.forZone(lodge.getZone()));
        } else {
            photo.setImage(ResortArt.image("zone-lagoon"));
        }

        guestTitle.setText(stay.getGuestName() + "  ·  Lodge " + stay.getUnitCode()
                + (lodge != null ? " (" + lodge.getZone() + " ZONE)" : ""));
        meta.setText("Ref: " + stay.getBookingReference()
                + "  ·  Dates: " + DateUtils.display(stay.getCheckIn()) + " to " + DateUtils.display(stay.getCheckOut())
                + "  ·  Status: " + stay.getStatus().name()
                + "  ·  Total: " + CurrencyUtils.format(stay.getTotal()));

        header.setHook(stay.getGuestName() + "  ·  " + stay.getStatus().name() + "  ·  "
                + (currentGuest != null && currentGuest.isAadharVerified() ? "Aadhaar Verified" : "Aadhaar Pending"));

        renderAadharSection();
    }

    private void renderAadharSection() {
        aadharContainer.getChildren().clear();
        if (currentGuest == null) {
            return;
        }

        Label aadharHeading = new Label("Customer Identity Verification (Aadhaar KYC)");
        aadharHeading.setStyle("-fx-font-weight: bold; -fx-text-fill: #1B2430; -fx-padding: 4 0 0 0;");

        CheckBox aadharCheckBox = new CheckBox("Aadhaar Card Verified / Validated");
        aadharCheckBox.setStyle("-fx-font-weight: bold;");
        aadharCheckBox.setSelected(currentGuest.isAadharVerified());
        aadharCheckBox.selectedProperty().addListener((obs, was, isNow) -> {
            currentGuest.setAadharVerified(isNow);
            guestService.updateAadhar(currentGuest.getId(), isNow, currentGuest.getAadharImagePath());
        });

        String docNum = currentGuest.getIdDocNumber();
        Label docLabel = new Label("Aadhaar Number: " + (docNum != null && !docNum.isBlank() ? docNum : "Not recorded"));
        docLabel.setStyle("-fx-text-fill: #5C6B73; -fx-font-size: 12px;");

        StackPane thumbPane = new StackPane();
        Runnable updateThumbnail = () -> {
            thumbPane.getChildren().clear();
            var thumb = AadharUtils.createThumbnail(currentGuest.getAadharImagePath(), 140, 85, () -> {
                var img = AadharUtils.loadAadharImage(currentGuest.getAadharImagePath());
                if (img != null) {
                    AadharUtils.showViewer(Main.stage(), "Aadhaar Card · " + currentGuest.getFullName(), img);
                }
            });
            thumbPane.getChildren().add(thumb);
        };
        updateThumbnail.run();

        Button uploadBtn = new Button("Upload Aadhaar Image");
        uploadBtn.getStyleClass().add("secondary");
        uploadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose Customer Aadhaar Card Photo");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images (*.png, *.jpg, *.jpeg, *.webp)", "*.png", "*.jpg", "*.jpeg", "*.webp")
            );
            File file = chooser.showOpenDialog(Main.stage());
            if (file != null) {
                try {
                    String path = AadharUtils.saveUploadedAadhar(currentGuest.getId(), file);
                    currentGuest.setAadharImagePath(path);
                    currentGuest.setAadharVerified(true);
                    aadharCheckBox.setSelected(true);
                    guestService.updateAadhar(currentGuest.getId(), true, path);
                    updateThumbnail.run();
                } catch (Exception ex) {
                    Fx.error(ex);
                }
            }
        });

        Button sampleBtn = new Button("Use Sample Aadhaar");
        sampleBtn.getStyleClass().add("secondary");
        sampleBtn.setOnAction(e -> {
            currentGuest.setAadharImagePath("sample-aadhar.png");
            currentGuest.setAadharVerified(true);
            aadharCheckBox.setSelected(true);
            guestService.updateAadhar(currentGuest.getId(), true, "sample-aadhar.png");
            updateThumbnail.run();
        });

        HBox actionRow = new HBox(12, thumbPane, new VBox(6, aadharCheckBox, docLabel, new HBox(8, uploadBtn, sampleBtn)));
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(6, 12, 6, 12));
        actionRow.setStyle("-fx-background-color: #F7F4EF; -fx-background-radius: 6; -fx-border-color: #E4DDD2; -fx-border-radius: 6;");

        aadharContainer.getChildren().addAll(aadharHeading, actionRow);
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
