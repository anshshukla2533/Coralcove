package coralcove.ui.lodgecare;

import coralcove.model.Employee;
import coralcove.model.Lodge;
import coralcove.model.LodgeCareTask;
import coralcove.model.Role;
import coralcove.service.LodgeCareService;
import coralcove.service.Session;
import coralcove.ui.Columns;
import coralcove.ui.Dialogs;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ScreenHeader;
import javafx.scene.layout.HBox;
import coralcove.util.AppException;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class LodgeCareView extends VBox implements Refreshable {
    private static final String[] PRIORITY = {"LOW", "NORMAL", "HIGH"};
    private static final String[] STATUS = {"PENDING", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "INSPECTION_REQUIRED"};
    private static final String[] INSPECTION = {"NONE", "PASSED", "FAILED"};

    private final LodgeCareService care = new LodgeCareService();
    private final ComboBox<String> statusFilter = new ComboBox<>();
    private final ComboBox<String> priorityFilter = new ComboBox<>();
    private final TableView<LodgeCareTask> tasks = new TableView<>();
    private final TableView<String[]> repairs = Columns.strings("Id", "Lodge", "Description", "Status", "Opened");
    private final ScreenHeader header = new ScreenHeader("Lodge care", "Housekeeping tasks and estate repairs.");
    private final HBox pulse = new HBox(8);

    public LodgeCareView() {
        setSpacing(10);
        statusFilter.getItems().add("");
        statusFilter.getItems().addAll(STATUS);
        priorityFilter.getItems().add("");
        priorityFilter.getItems().addAll(PRIORITY);
        statusFilter.getSelectionModel().select(0);
        priorityFilter.getSelectionModel().select(0);
        tasks.getColumns().add(Columns.of("Lodge", LodgeCareTask::getUnitCode));
        tasks.getColumns().add(Columns.of("Priority", LodgeCareTask::getPriority));
        tasks.getColumns().add(Columns.of("Status", LodgeCareTask::getStatus));
        tasks.getColumns().add(Columns.of("Inspection", LodgeCareTask::getInspectionStatus));
        tasks.getColumns().add(Columns.of("Assignee", LodgeCareTask::getAssigneeName));
        tasks.getColumns().add(Columns.of("Notes", LodgeCareTask::getNotes));
        tasks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        boolean write = canWrite();
        Button refresh = button("Refresh", false);
        refresh.setOnAction(event -> refresh());
        Button create = button("New task", true);
        create.setDisable(!write);
        create.setOnAction(event -> createTask());
        Button update = button("Update task", false);
        update.setDisable(!write);
        update.setOnAction(event -> updateTask());
        Button repair = button("Report repair", false);
        repair.setDisable(!write);
        repair.setOnAction(event -> reportRepair());
        Button resolve = button("Resolve repair", false);
        resolve.setDisable(!write);
        resolve.setOnAction(event -> resolveRepair());
        VBox.setVgrow(tasks, Priority.ALWAYS);
        repairs.setPrefHeight(180);
        getChildren().addAll(
                header,
                pulse,
                Fx.bar(new Label("Status"), statusFilter, new Label("Priority"), priorityFilter, refresh, create, update),
                tasks,
                new Label("Estate repairs"),
                repairs,
                Fx.bar(repair, resolve));
    }

    @Override
    public void refresh() {
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();
        Fx.run(() -> new Object[] {care.search(status, priority), care.openMaintenance()}, loaded -> {
            java.util.List<LodgeCareTask> rows = (java.util.List<LodgeCareTask>) loaded[0];
            tasks.getItems().setAll(rows);
            Columns.fill(repairs, (java.util.List<String[]>) loaded[1]);
            int high = 0;
            int progress = 0;
            int inspect = 0;
            for (LodgeCareTask task : rows) {
                if ("HIGH".equals(task.getPriority())) {
                    high++;
                }
                if ("IN_PROGRESS".equals(task.getStatus())) {
                    progress++;
                }
                if ("INSPECTION_REQUIRED".equals(task.getStatus()) || "FAILED".equals(task.getInspectionStatus())) {
                    inspect++;
                }
            }
            pulse.getChildren().setAll(
                    Fx.chip(high + " high", "high"),
                    Fx.chip(progress + " in progress", "in-stay"),
                    Fx.chip(inspect + " inspection", inspect > 0 ? "held" : "ready"));
            header.refreshPhoto();
            header.setHook(rows.size() + " open tasks  ·  " + ((java.util.List<?>) loaded[1]).size() + " estate repairs");
        });
    }

    private void createTask() {
        Fx.run(() -> new Object[] {care.lodges(), care.staff()}, loaded -> {
            ComboBox<Lodge> lodge = new ComboBox<>();
            lodge.getItems().setAll((java.util.List<Lodge>) loaded[0]);
            ComboBox<Employee> staff = new ComboBox<>();
            staff.getItems().setAll((java.util.List<Employee>) loaded[1]);
            ComboBox<String> priority = new ComboBox<>();
            priority.getItems().addAll(PRIORITY);
            priority.getSelectionModel().select("NORMAL");
            TextField notes = new TextField();
            GridPane form = new GridPane();
            form.add(new Label("Lodge"), 0, 0);
            form.add(lodge, 1, 0);
            form.add(new Label("Attendant"), 0, 1);
            form.add(staff, 1, 1);
            form.add(new Label("Priority"), 0, 2);
            form.add(priority, 1, 2);
            form.add(new Label("Notes"), 0, 3);
            form.add(notes, 1, 3);
            Object[] picked = new Object[3];
            Dialogs.form("Lodge care", form, () -> {
                if (lodge.getValue() == null) {
                    throw new AppException("Choose a lodge.");
                }
                picked[0] = lodge.getValue();
                picked[1] = staff.getValue();
                picked[2] = priority.getValue() + "\n" + notes.getText();
            });
            if (picked[0] == null) {
                return;
            }
            Lodge chosen = (Lodge) picked[0];
            Employee assignee = (Employee) picked[1];
            String packed = (String) picked[2];
            String level = packed.substring(0, packed.indexOf('\n'));
            String note = packed.substring(packed.indexOf('\n') + 1);
            Long assigneeId = assignee == null ? null : assignee.getId();
            Fx.run(() -> {
                care.create(chosen.getId(), assigneeId, level, note);
                return null;
            }, ignored -> refresh());
        });
    }

    private void updateTask() {
        LodgeCareTask task = tasks.getSelectionModel().getSelectedItem();
        if (task == null) {
            return;
        }
        ComboBox<String> priority = new ComboBox<>();
        priority.getItems().addAll(PRIORITY);
        priority.getSelectionModel().select(task.getPriority());
        ComboBox<String> status = new ComboBox<>();
        status.getItems().addAll(STATUS);
        status.getSelectionModel().select(task.getStatus());
        ComboBox<String> inspection = new ComboBox<>();
        inspection.getItems().addAll(INSPECTION);
        inspection.getSelectionModel().select(task.getInspectionStatus());
        TextField notes = new TextField(task.getNotes() == null ? "" : task.getNotes());
        GridPane form = new GridPane();
        form.add(new Label("Priority"), 0, 0);
        form.add(priority, 1, 0);
        form.add(new Label("Status"), 0, 1);
        form.add(status, 1, 1);
        form.add(new Label("Inspection"), 0, 2);
        form.add(inspection, 1, 2);
        form.add(new Label("Notes"), 0, 3);
        form.add(notes, 1, 3);
        String[] picked = new String[1];
        Dialogs.form("Update task", form, () -> picked[0] = priority.getValue() + "|" + status.getValue()
                + "|" + inspection.getValue() + "|" + notes.getText());
        if (picked[0] == null) {
            return;
        }
        String[] parts = picked[0].split("\\|", 4);
        Fx.run(() -> {
            care.update(task.getId(), task.getAssigneeId(), parts[0], parts[1], parts[2], parts[3]);
            return null;
        }, ignored -> refresh());
    }

    private void reportRepair() {
        Fx.run(care::lodges, lodges -> {
            ComboBox<Lodge> lodge = new ComboBox<>();
            lodge.getItems().setAll(lodges);
            TextField description = new TextField();
            GridPane form = new GridPane();
            form.add(new Label("Lodge"), 0, 0);
            form.add(lodge, 1, 0);
            form.add(new Label("Description"), 0, 1);
            form.add(description, 1, 1);
            Object[] picked = new Object[2];
            Dialogs.form("Estate repair", form, () -> {
                if (lodge.getValue() == null) {
                    throw new AppException("Choose a lodge.");
                }
                picked[0] = lodge.getValue();
                picked[1] = description.getText();
            });
            if (picked[0] == null) {
                return;
            }
            Lodge chosen = (Lodge) picked[0];
            String text = (String) picked[1];
            Fx.run(() -> {
                care.reportMaintenance(chosen.getId(), text);
                return null;
            }, ignored -> refresh());
        });
    }

    private void resolveRepair() {
        String[] row = repairs.getSelectionModel().getSelectedItem();
        if (row == null) {
            return;
        }
        Fx.run(care::lodges, lodges -> {
            Lodge match = lodges.stream().filter(lodge -> lodge.getUnitCode().equals(row[1])).findFirst().orElse(null);
            if (match == null) {
                Fx.error(new AppException("Lodge " + row[1] + " was not found in this resort."));
                return;
            }
            long id = Long.parseLong(row[0]);
            Fx.run(() -> {
                care.resolveMaintenance(id, match.getId());
                return null;
            }, ignored -> refresh());
        });
    }

    private static boolean canWrite() {
        Role role = Session.get() == null ? null : Session.get().getRole();
        return role == Role.ADMIN || role == Role.MANAGER || role == Role.ATTENDANT;
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
