package coralcove.ui.employees;

import coralcove.model.Employee;
import coralcove.service.EmployeeService;
import coralcove.ui.Columns;
import coralcove.ui.Dialogs;
import coralcove.ui.Fx;
import coralcove.ui.Refreshable;
import coralcove.ui.ScreenHeader;
import javafx.scene.layout.HBox;
import coralcove.util.AppException;
import coralcove.util.CurrencyUtils;
import coralcove.util.DateUtils;
import coralcove.util.Money;
import java.time.LocalDate;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class EmployeeView extends VBox implements Refreshable {
    private final EmployeeService staff = new EmployeeService();
    private final TextField search = new TextField();
    private final TextField department = new TextField();
    private final DatePicker day = new DatePicker(DateUtils.today());
    private final TableView<Employee> people = new TableView<>();
    private final TableView<String[]> attendance = Columns.strings("Employee", "Department", "Status", "Note");
    private final ScreenHeader header = new ScreenHeader("Employees", "Staff and today's attendance.");
    private final HBox pulse = new HBox(8);

    public EmployeeView() {
        setSpacing(10);
        Fx.dates(day);
        search.setPromptText("Name");
        department.setPromptText("Department");
        people.getColumns().add(Columns.of("Name", Employee::getName));
        people.getColumns().add(Columns.of("Department", Employee::getDepartment));
        people.getColumns().add(Columns.of("Designation", Employee::getDesignation));
        people.getColumns().add(Columns.of("Phone", Employee::getPhone));
        people.getColumns().add(Columns.of("Status", Employee::getStatus));
        people.getColumns().add(Columns.of("Salary", employee -> CurrencyUtils.format(employee.getSalary())));
        people.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Button find = button("Search", false);
        find.setOnAction(event -> refresh());
        Button add = button("New employee", true);
        add.setOnAction(event -> edit(null));
        Button edit = button("Edit", false);
        edit.setOnAction(event -> {
            Employee employee = people.getSelectionModel().getSelectedItem();
            if (employee == null) {
                Fx.info("Select an employee from the table first.");
                return;
            }
            edit(employee);
        });
        Button mark = button("Mark attendance", false);
        mark.setOnAction(event -> mark());
        VBox.setVgrow(people, Priority.ALWAYS);
        people.setMinHeight(220);
        people.setPrefHeight(260);
        attendance.setMinHeight(180);
        attendance.setPrefHeight(200);
        getChildren().addAll(header, pulse, Fx.bar(search, department, find, add, edit), people,
                Fx.bar(new Label("Attendance"), day, mark), attendance);
    }

    @Override
    public void refresh() {
        String text = Fx.text(search);
        String dept = Fx.text(department);
        LocalDate date = day.getValue() == null ? DateUtils.today() : day.getValue();
        Fx.run(() -> new Object[] {staff.search(text, dept), staff.attendanceOn(DateUtils.iso(date))}, loaded -> {
            java.util.List<Employee> roster = (java.util.List<Employee>) loaded[0];
            java.util.List<String[]> marks = (java.util.List<String[]>) loaded[1];
            people.getItems().setAll(roster);
            Columns.fill(attendance, marks);
            int present = 0;
            int absent = 0;
            int leave = 0;
            for (String[] row : marks) {
                String state = row.length > 2 ? row[2] : "";
                if ("PRESENT".equalsIgnoreCase(state)) {
                    present++;
                } else if ("ABSENT".equalsIgnoreCase(state)) {
                    absent++;
                } else if ("LEAVE".equalsIgnoreCase(state)) {
                    leave++;
                }
            }
            pulse.getChildren().setAll(
                    Fx.chip(present + " present", "present"),
                    Fx.chip(absent + " absent", "held"),
                    Fx.chip(leave + " leave", "turndown"));
            header.refreshPhoto();
            header.setHook(roster.size() + " on the roster  ·  " + marks.size() + " marked for this date");
        });
    }

    private void edit(Employee existing) {
        TextField name = new TextField(existing == null ? "" : existing.getName());
        TextField phone = new TextField(existing == null ? "" : empty(existing.getPhone()));
        TextField email = new TextField(existing == null ? "" : empty(existing.getEmail()));
        TextField designation = new TextField(existing == null ? "" : empty(existing.getDesignation()));
        TextField dept = new TextField(existing == null ? "" : empty(existing.getDepartment()));
        TextField salary = new TextField(existing == null ? "0" : existing.getSalary().toPlainString());
        TextField joined = new TextField(existing == null ? DateUtils.display(DateUtils.today()) : empty(existing.getJoiningDate()));
        ComboBox<String> state = new ComboBox<>();
        state.getItems().addAll("ACTIVE", "INACTIVE");
        state.getSelectionModel().select(existing == null ? "ACTIVE" : existing.getStatus());
        GridPane form = new GridPane();
        form.add(new Label("Name"), 0, 0);
        form.add(name, 1, 0);
        form.add(new Label("Phone"), 0, 1);
        form.add(phone, 1, 1);
        form.add(new Label("Email"), 0, 2);
        form.add(email, 1, 2);
        form.add(new Label("Designation"), 0, 3);
        form.add(designation, 1, 3);
        form.add(new Label("Department"), 0, 4);
        form.add(dept, 1, 4);
        form.add(new Label("Salary"), 0, 5);
        form.add(salary, 1, 5);
        form.add(new Label("Joined"), 0, 6);
        form.add(joined, 1, 6);
        form.add(new Label("Status"), 0, 7);
        form.add(state, 1, 7);
        Employee[] draft = new Employee[1];
        Dialogs.form(existing == null ? "Employee" : "Edit employee", form, () -> {
            Employee employee = existing == null ? new Employee() : existing;
            employee.setName(name.getText());
            employee.setPhone(phone.getText());
            employee.setEmail(email.getText());
            employee.setDesignation(designation.getText());
            employee.setDepartment(dept.getText());
            employee.setSalary(Money.of(salary.getText().isBlank() ? "0" : salary.getText()));
            employee.setJoiningDate(joined.getText());
            employee.setStatus(state.getValue());
            draft[0] = employee;
        });
        if (draft[0] != null) {
            boolean create = existing == null;
            Fx.run(() -> staff.save(draft[0], create), saved -> refresh());
        }
    }

    private void mark() {
        Employee employee = people.getSelectionModel().getSelectedItem();
        if (employee == null) {
            Fx.error(new AppException("Select an employee first."));
            return;
        }
        ComboBox<String> state = new ComboBox<>();
        state.getItems().addAll("PRESENT", "ABSENT", "LEAVE");
        state.getSelectionModel().select(0);
        TextField note = new TextField();
        GridPane form = new GridPane();
        form.add(new Label("Status"), 0, 0);
        form.add(state, 1, 0);
        form.add(new Label("Note"), 0, 1);
        form.add(note, 1, 1);
        String[] picked = new String[1];
        Dialogs.form("Attendance", form, () -> picked[0] = state.getValue() + "|" + note.getText());
        if (picked[0] == null) {
            return;
        }
        String[] parts = picked[0].split("\\|", 2);
        LocalDate date = day.getValue() == null ? DateUtils.today() : day.getValue();
        Fx.run(() -> {
            staff.attendance(employee.getId(), DateUtils.iso(date), parts[0], parts.length > 1 ? parts[1] : "");
            return null;
        }, ignored -> refresh());
    }

    private static String empty(String value) {
        return value == null ? "" : value;
    }

    private static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "primary" : "secondary");
        return button;
    }
}
