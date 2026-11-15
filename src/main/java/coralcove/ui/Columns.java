package coralcove.ui;

import java.util.List;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class Columns {
    private Columns() {
    }

    public static <T> TableColumn<T, String> of(String title, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(blank(value.apply(data.getValue()))));
        return column;
    }

    public static TableView<String[]> strings(String... titles) {
        TableView<String[]> table = new TableView<>();
        for (int i = 0; i < titles.length; i++) {
            int index = i;
            TableColumn<String[], String> column = new TableColumn<>(titles[i]);
            column.setCellValueFactory(data -> {
                String[] row = data.getValue();
                String value = row != null && index < row.length ? row[index] : "";
                return new SimpleStringProperty(blank(value));
            });
            table.getColumns().add(column);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return table;
    }

    public static void fill(TableView<String[]> table, List<String[]> rows) {
        table.getItems().setAll(rows == null ? List.of() : rows);
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }
}
