package coralcove.ui.billing;

import coralcove.model.Invoice;
import coralcove.model.Payment;
import coralcove.model.PaymentMethod;
import coralcove.service.BillingService;
import coralcove.ui.Columns;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ScreenHeader;
import javafx.scene.control.Label;
import coralcove.util.CurrencyUtils;
import coralcove.util.Money;
import coralcove.util.PDFGenerator;
import coralcove.util.ValidationUtils;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public final class BillingView extends VBox implements Refreshable {
    private final BillingService billing = new BillingService();
    private final TextField guest = new TextField();
    private final ComboBox<String> status = new ComboBox<>();
    private final TableView<Invoice> invoices = new TableView<>();
    private final TableView<String[]> items = Columns.strings("Kind", "Description", "Qty", "Price", "Line");
    private final TableView<Payment> payments = new TableView<>();
    private final TextField description = new TextField();
    private final TextField qty = new TextField("1");
    private final TextField price = new TextField();
    private final TextField amount = new TextField();
    private final TextField reference = new TextField();
    private final TextField discount = new TextField();
    private final ComboBox<String> kind = new ComboBox<>();
    private final ComboBox<PaymentMethod> method = new ComboBox<>();
    private final ScreenHeader header = new ScreenHeader("Billing", "Invoices, charges, and payments.");
    private final Label heroTitle = new Label("Select an invoice.");
    private final Label heroBalance = new Label("");

    public BillingView() {
        setSpacing(10);
        guest.setPromptText("Guest");
        status.getItems().addAll("", "UNPAID", "PARTIALLY_PAID", "PAID", "REFUNDED");
        status.getSelectionModel().select(0);
        kind.getItems().addAll("RESTAURANT", "LAUNDRY", "MINIBAR", "MISC");
        kind.getSelectionModel().select(0);
        method.getItems().setAll(PaymentMethod.values());
        method.getSelectionModel().select(PaymentMethod.CASH);
        description.setPromptText("Description");
        price.setPromptText("Price");
        amount.setPromptText("Amount");
        reference.setPromptText("Reference");
        discount.setPromptText("Discount");
        invoices.getColumns().add(Columns.of("Number", Invoice::getInvoiceNumber));
        invoices.getColumns().add(Columns.of("Guest", Invoice::getGuestName));
        invoices.getColumns().add(Columns.of("Total", invoice -> CurrencyUtils.format(invoice.getTotal())));
        invoices.getColumns().add(Columns.of("Paid", invoice -> CurrencyUtils.format(invoice.getAmountPaid())));
        invoices.getColumns().add(Columns.of("Balance", invoice -> CurrencyUtils.format(invoice.getBalance())));
        invoices.getColumns().add(Columns.of("Status", invoice -> invoice.getStatus().name()));
        invoices.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        invoices.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> loadDetail());
        payments.getColumns().add(Columns.of("When", Payment::getPaidAt));
        payments.getColumns().add(Columns.of("Method", payment -> payment.getMethod().label()));
        payments.getColumns().add(Columns.of("Reference", Payment::getReference));
        payments.getColumns().add(Columns.of("Amount", payment -> CurrencyUtils.format(payment.getAmount())));
        payments.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Button search = button("Search", false);
        search.setOnAction(event -> refresh());
        Button add = button("Add charge", true);
        add.setOnAction(event -> addCharge());
        Button pay = button("Take payment", true);
        pay.setOnAction(event -> pay(false));
        Button refund = button("Refund", false);
        refund.setOnAction(event -> pay(true));
        Button applyDiscount = button("Apply discount", false);
        applyDiscount.setOnAction(event -> applyDiscount());
        Button pdf = button("Save PDF", false);
        pdf.setOnAction(event -> savePdf());
        VBox.setVgrow(invoices, Priority.ALWAYS);
        HBox detail = new HBox(8, items, payments);
        HBox.setHgrow(items, Priority.ALWAYS);
        HBox.setHgrow(payments, Priority.ALWAYS);
        items.setPrefHeight(180);
        payments.setPrefHeight(180);
        heroTitle.getStyleClass().add("panel-title");
        heroBalance.getStyleClass().add("hero-balance");
        VBox hero = new VBox(4, heroTitle, heroBalance);
        hero.getStyleClass().add("hero-banner");
        getChildren().addAll(
                header,
                hero,
                Fx.bar(guest, status, search),
                invoices,
                detail,
                Fx.bar(kind, description, qty, price, add),
                Fx.bar(amount, method, reference, pay, refund, discount, applyDiscount, pdf));
    }

    @Override
    public void refresh() {
        String who = Fx.text(guest);
        String state = status.getValue();
        Fx.run(() -> billing.list(who, state), found -> {
            invoices.getItems().setAll(found);
            if (!found.isEmpty()) {
                invoices.getSelectionModel().select(0);
            } else {
                Columns.fill(items, List.of());
                payments.getItems().clear();
            }
        });
    }

    private void loadDetail() {
        Invoice invoice = invoices.getSelectionModel().getSelectedItem();
        if (invoice == null) {
            heroTitle.setText("Select an invoice.");
            heroBalance.setText("");
            return;
        }
        heroTitle.setText(invoice.getInvoiceNumber() + "  ·  " + invoice.getGuestName() + "  ·  " + invoice.getStatus().name());
        heroBalance.setText("Balance " + CurrencyUtils.format(invoice.getBalance()));
        header.refreshPhoto();
        long id = invoice.getId();
        Fx.run(() -> new Object[] {billing.items(id), billing.payments(id)}, loaded -> {
            Columns.fill(items, (List<String[]>) loaded[0]);
            payments.getItems().setAll((List<Payment>) loaded[1]);
        });
    }

    private void addCharge() {
        Invoice invoice = invoices.getSelectionModel().getSelectedItem();
        if (invoice == null) {
            return;
        }
        String lineKind = kind.getValue();
        String text = Fx.text(description);
        String quantity = Fx.text(qty);
        String unit = Fx.text(price);
        Fx.run(() -> {
            billing.addCharge(invoice.getId(), lineKind, text,
                    ValidationUtils.positiveInt(quantity, "Quantity"),
                    ValidationUtils.money(unit, "Price", false));
            return null;
        }, ignored -> refresh());
    }

    private void pay(boolean refund) {
        Invoice invoice = invoices.getSelectionModel().getSelectedItem();
        if (invoice == null) {
            return;
        }
        String raw = Fx.text(amount);
        String ref = Fx.text(reference);
        PaymentMethod how = method.getValue();
        Fx.run(() -> {
            billing.takePayment(invoice.getId(), ValidationUtils.money(raw, "Amount", false), how, ref, refund);
            return null;
        }, ignored -> refresh());
    }

    private void applyDiscount() {
        Invoice invoice = invoices.getSelectionModel().getSelectedItem();
        if (invoice == null) {
            return;
        }
        String raw = Fx.text(discount);
        Fx.run(() -> {
            billing.setDiscount(invoice.getId(), Money.of(raw.isBlank() ? "0" : raw));
            return null;
        }, ignored -> refresh());
    }

    private void savePdf() {
        Invoice invoice = invoices.getSelectionModel().getSelectedItem();
        if (invoice == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(invoice.getInvoiceNumber() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        var file = chooser.showSaveDialog(coralcove.Main.stage());
        if (file == null) {
            return;
        }
        long id = invoice.getId();
        Fx.run(() -> billing.items(id), lines -> {
            List<String> body = new ArrayList<>();
            body.add(invoice.getGuestName());
            body.add("Total " + CurrencyUtils.formatPlain(invoice.getTotal()));
            body.add("Paid " + CurrencyUtils.formatPlain(invoice.getAmountPaid()));
            body.add("Balance " + CurrencyUtils.formatPlain(invoice.getBalance()));
            for (String[] line : lines) {
                body.add(line[0] + "  " + line[1] + "  " + line[2] + " x Rs. " + line[3]);
            }
            PDFGenerator.write(file.toPath(), invoice.getInvoiceNumber(), body);
            Fx.info("Saved " + file.getName());
        });
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
