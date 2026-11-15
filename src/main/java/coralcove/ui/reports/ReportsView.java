package coralcove.ui.reports;

import coralcove.service.ReportService;
import coralcove.ui.Columns;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ScreenHeader;
import coralcove.util.CSVExporter;
import coralcove.util.DateUtils;
import coralcove.util.PDFGenerator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public final class ReportsView extends VBox implements Refreshable {
    static final List<String> NAMES = List.of(
            "Daily revenue",
            "Monthly revenue",
            "Yearly revenue",
            "Occupancy rate",
            "Stay trends",
            "Cancellation rate",
            "Guest history",
            "Outstanding payments",
            "Lodge performance",
            "Inventory status",
            "Employee attendance");

    private final ReportService reports = new ReportService();
    private final ComboBox<String> kind = new ComboBox<>();
    private final DatePicker from = new DatePicker(DateUtils.today().minusDays(30));
    private final DatePicker to = new DatePicker(DateUtils.today());
    private final Label note = new Label();
    private final Label picker = new Label();
    private final TableView<String[]> table = new TableView<>();
    private final ScreenHeader header = new ScreenHeader("Reports", "Eleven ledgers for the front office.");
    private ReportService.Table last;

    public ReportsView() {
        setSpacing(10);
        kind.getItems().setAll(NAMES);
        kind.getSelectionModel().select(0);
        Fx.dates(from);
        Fx.dates(to);
        note.setWrapText(true);
        note.getStyleClass().add("muted");
        picker.setWrapText(true);
        picker.getStyleClass().add("muted");
        kind.valueProperty().addListener((obs, a, b) -> picker.setText(copy(b)));
        picker.setText(copy(kind.getValue()));
        Button run = button("Run", true);
        run.setOnAction(event -> refresh());
        Button csv = button("Export CSV", false);
        csv.setOnAction(event -> exportCsv());
        Button pdf = button("Save PDF", false);
        pdf.setOnAction(event -> exportPdf());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(header, Fx.bar(kind, from, to, run, csv, pdf), picker, note, table);
    }

    @Override
    public void refresh() {
        String name = kind.getValue();
        LocalDate start = from.getValue() == null ? DateUtils.today().minusDays(30) : from.getValue();
        LocalDate end = to.getValue() == null ? DateUtils.today() : to.getValue();
        Fx.run(() -> reports.run(name, start, end), this::show);
    }

    private void show(ReportService.Table result) {
        last = result;
        note.setText(result.note());
        header.refreshPhoto();
        header.setHook(result.title() + "  ·  " + result.rows().size() + " rows");
        table.getColumns().clear();
        for (int i = 0; i < result.columns().size(); i++) {
            int index = i;
            var column = Columns.of(result.columns().get(i), (String[] row) -> index < row.length ? row[index] : "");
            table.getColumns().add(column);
        }
        List<String[]> rows = new ArrayList<>();
        for (List<String> row : result.rows()) {
            rows.add(row.toArray(String[]::new));
        }
        Columns.fill(table, rows);
    }

    private void exportCsv() {
        if (last == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(last.title().replace(' ', '-') + ".csv");
        var file = chooser.showSaveDialog(coralcove.Main.stage());
        if (file == null) {
            return;
        }
        CSVExporter.write(file.toPath(), last.columns(), last.rows());
        Fx.info("Saved " + file.getName());
    }

    private void exportPdf() {
        if (last == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(last.title().replace(' ', '-') + ".pdf");
        var file = chooser.showSaveDialog(coralcove.Main.stage());
        if (file == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        lines.add(last.note());
        lines.add(String.join(" | ", last.columns()));
        for (List<String> row : last.rows()) {
            lines.add(String.join(" | ", row).replace("\u20B9", "Rs. "));
        }
        PDFGenerator.write(file.toPath(), last.title(), lines);
        Fx.info("Saved " + file.getName());
    }

    private static String copy(String name) {
        if (name == null) {
            return "";
        }
        return switch (name) {
            case "Daily revenue" -> "Nightly take by stay, dining, and extras.";
            case "Monthly revenue" -> "Month roll-up for the selected window.";
            case "Yearly revenue" -> "Year totals for the board pack.";
            case "Occupancy rate" -> "Sold nights against inventory.";
            case "Stay trends" -> "Arrivals and length of stay over the dates.";
            case "Cancellation rate" -> "Cancelled stays against bookings opened.";
            case "Guest history" -> "Repeat guests and lifetime spend.";
            case "Outstanding payments" -> "Open invoices still on the books.";
            case "Lodge performance" -> "Revenue and occupancy by unit.";
            case "Inventory status" -> "Stock versus minimums.";
            case "Employee attendance" -> "Present, absent, and leave for the range.";
            default -> "";
        };
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
