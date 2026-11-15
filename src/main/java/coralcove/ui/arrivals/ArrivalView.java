package coralcove.ui.arrivals;

import coralcove.model.StayBooking;
import coralcove.service.ArrivalService;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ResortArt;
import coralcove.ui.ScreenHeader;
import coralcove.util.CurrencyUtils;
import coralcove.util.DateUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class ArrivalView extends VBox implements Refreshable {
    private final ArrivalService arrivals = new ArrivalService();
    private final TextField reference = new TextField();
    private final ScreenHeader header = new ScreenHeader("Arrivals", "Find a booking and walk the guest in or out.");
    private final ImageView photo = new ImageView(ResortArt.image("zone-beach"));
    private final Label guest = new Label("Enter a booking reference.");
    private final Label meta = new Label();
    private StayBooking current;

    public ArrivalView() {
        setSpacing(10);
        reference.setPromptText("Booking reference");
        Button find = button("Find", false);
        find.setOnAction(event -> lookup());
        Button in = button("Check in", true);
        in.setOnAction(event -> act(true));
        Button out = button("Check out", false);
        out.setOnAction(event -> act(false));
        photo.setFitWidth(280);
        photo.setFitHeight(160);
        photo.setPreserveRatio(false);
        guest.getStyleClass().add("panel-title");
        guest.setWrapText(true);
        meta.getStyleClass().add("muted");
        meta.setWrapText(true);
        VBox copy = new VBox(8, guest, meta);
        copy.setPadding(new Insets(16));
        HBox.setHgrow(copy, Priority.ALWAYS);
        HBox card = new HBox(0, photo, copy);
        card.getStyleClass().add("stay-card");
        card.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(header, Fx.bar(reference, find, in, out), card);
    }

    @Override
    public void refresh() {
        header.refreshPhoto();
        if (!Fx.text(reference).isBlank()) {
            lookup();
        }
    }

    private void lookup() {
        String ref = Fx.text(reference);
        Fx.run(() -> arrivals.findByReference(ref), stay -> show(stay), Fx::error);
    }

    private void act(boolean checkIn) {
        if (current == null) {
            guest.setText("Find a stay first.");
            meta.setText("");
            return;
        }
        long id = current.getId();
        Fx.run(() -> checkIn ? arrivals.checkIn(id) : arrivals.checkOut(id), this::show);
    }

    private void show(StayBooking stay) {
        current = stay;
        if (stay == null) {
            guest.setText("No stay matches that reference.");
            meta.setText("");
            photo.setImage(ResortArt.image("zone-beach"));
            return;
        }
        guest.setText(stay.getGuestName());
        meta.setText(stay.getBookingReference() + "  ·  " + stay.getUnitCode() + "\n"
                + DateUtils.display(stay.getCheckIn()) + " to " + DateUtils.display(stay.getCheckOut()) + "\n"
                + stay.getStatus().name() + "  ·  " + CurrencyUtils.format(stay.getTotal()));
        photo.setImage(ResortArt.image("zone-beach"));
        header.setHook(stay.getGuestName() + "  ·  " + stay.getStatus().name());
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
