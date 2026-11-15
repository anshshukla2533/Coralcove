package coralcove.ui.dashboard;

import coralcove.service.DashboardService;
import coralcove.ui.Columns;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ScreenHeader;
import java.math.BigDecimal;
import java.util.Map;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class DashboardView extends VBox implements Refreshable {
    private final DashboardService dashboard = new DashboardService();
    private final Label rooms = stat("Lodges");
    private final Label available = stat("Ready");
    private final Label occupied = stat("In stay");
    private final Label reserved = stat("Held");
    private final Label arrivals = stat("Arrivals");
    private final Label departures = stat("Departures");
    private final Label revenue = stat("Revenue today");
    private final Label guests = stat("Guests");
    private final ScreenHeader header = new ScreenHeader("Desk", "Today at the selected resort.");
    private final VBox charts = new VBox(8);
    private final TableView<String[]> arrivalTable = Columns.strings("Stay", "Guest", "Lodge", "Status");
    private final TableView<String[]> departureTable = Columns.strings("Stay", "Guest", "Lodge", "Status");

    public DashboardView() {
        setSpacing(12);
        GridPane cards = new GridPane();
        cards.setHgap(12);
        cards.setVgap(12);
        for (int column = 0; column < 4; column++) {
            ColumnConstraints constraint = new ColumnConstraints();
            constraint.setPercentWidth(25);
            constraint.setHgrow(Priority.ALWAYS);
            cards.getColumnConstraints().add(constraint);
        }
        Label[] values = {rooms, available, occupied, reserved, arrivals, departures, revenue, guests};
        String[] titles = {"Lodges", "Ready", "In stay", "Held", "Arrivals", "Departures", "Revenue today", "Guests"};
        for (int i = 0; i < values.length; i++) {
            Label caption = new Label(titles[i]);
            caption.getStyleClass().add("field-label");
            VBox card = new VBox(4, caption, values[i]);
            card.getStyleClass().addAll("card", "card-stat");
            card.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(card, Priority.ALWAYS);
            cards.add(card, i % 4, i / 4);
        }
        arrivalTable.setPrefHeight(160);
        departureTable.setPrefHeight(160);
        arrivalTable.setPlaceholder(emptyNote("No arrivals today."));
        departureTable.setPlaceholder(emptyNote("No departures today."));
        HBox lists = new HBox(12, titled("Today's arrivals", arrivalTable), titled("Today's departures", departureTable));
        HBox.setHgrow(lists.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(lists.getChildren().get(1), Priority.ALWAYS);
        getChildren().addAll(header, cards, charts, lists);
    }

    @Override
    public void refresh() {
        Fx.run(dashboard::load, this::paint);
    }

    private void paint(DashboardService.Snapshot snapshot) {
        rooms.setText(String.valueOf(snapshot.totalRooms));
        available.setText(String.valueOf(snapshot.available));
        occupied.setText(String.valueOf(snapshot.occupied));
        reserved.setText(String.valueOf(snapshot.reserved));
        arrivals.setText(String.valueOf(snapshot.arrivals.size()));
        departures.setText(String.valueOf(snapshot.departures.size()));
        revenue.setText(snapshot.revenueLabel());
        guests.setText(String.valueOf(snapshot.guests));
        Columns.fill(arrivalTable, snapshot.arrivals);
        Columns.fill(departureTable, snapshot.departures);
        header.refreshPhoto();
        header.setHook(snapshot.arrivals.size() + " arriving  ·  "
                + snapshot.departures.size() + " leaving  ·  "
                + snapshot.revenueLabel() + " taken today");
        charts.getChildren().clear();
        if (snapshot.monthlyRevenue.isEmpty() && snapshot.bookings.isEmpty() && snapshot.roomTypes.isEmpty()) {
            Label empty = new Label("No chart figures for this resort yet.");
            empty.getStyleClass().add("muted");
            charts.getChildren().add(empty);
            return;
        }
        if (!snapshot.monthlyRevenue.isEmpty()) {
            charts.getChildren().add(bars("Monthly revenue", snapshot.monthlyRevenue));
        }
        if (!snapshot.bookings.isEmpty()) {
            charts.getChildren().add(counts("Bookings", snapshot.bookings));
        }
        if (!snapshot.roomTypes.isEmpty()) {
            PieChart pie = new PieChart();
            pie.setTitle("Lodge categories");
            pie.setLegendVisible(false);
            pie.setPrefHeight(220);
            snapshot.roomTypes.forEach((name, count) -> pie.getData().add(new PieChart.Data(name, count)));
            charts.getChildren().add(pie);
        }
    }

    private BarChart<String, Number> bars(String title, Map<String, BigDecimal> series) {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setPrefHeight(220);
        XYChart.Series<String, Number> data = new XYChart.Series<>();
        series.forEach((key, value) -> data.getData().add(new XYChart.Data<>(key, value)));
        chart.getData().add(data);
        return chart;
    }

    private BarChart<String, Number> counts(String title, Map<String, Integer> series) {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setPrefHeight(220);
        XYChart.Series<String, Number> data = new XYChart.Series<>();
        series.forEach((key, value) -> data.getData().add(new XYChart.Data<>(key, value)));
        chart.getData().add(data);
        return chart;
    }

    private static Label stat(String ignored) {
        Label label = new Label("—");
        label.getStyleClass().add("stat-value");
        return label;
    }

    private static VBox titled(String title, TableView<String[]> table) {
        Label heading = new Label(title);
        heading.getStyleClass().add("panel-title");
        VBox box = new VBox(8, heading, table);
        box.getStyleClass().add("card");
        box.setMinWidth(0);
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private static Label emptyNote(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted");
        return label;
    }
}
