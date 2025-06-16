package com.hrsystem.hrsystem.controller;

import com.hrsystem.hrsystem.model.Employee;
import com.hrsystem.hrsystem.util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> idColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> departmentColumn;
    @FXML private TableColumn<Employee, Double> salaryColumn;
    @FXML private TableColumn<Employee, LocalDate> joinDateColumn; // NEW COLUMN
    @FXML private ComboBox<String> policyComboBox;
    @FXML private Button notificationButton;

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        joinDateColumn.setCellValueFactory(new PropertyValueFactory<>("joinDate")); // BIND NEW COLUMN

        loadEmployeeData();

        // Setup policy selector
        policyComboBox.setItems(FXCollections.observableArrayList("Yearly", "Half-Yearly"));
        policyComboBox.setValue("Yearly"); // Default value
    }

    private void loadEmployeeData() {
        employeeList.clear();
        String query = "SELECT * FROM employees ORDER BY id";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date joinSqlDate = rs.getDate("join_date");
                LocalDate joinLocalDate = (joinSqlDate != null) ? joinSqlDate.toLocalDate() : null;
                employeeList.add(new Employee(
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("department"), rs.getDouble("salary"), joinLocalDate
                ));
            }
            employeeTable.setItems(employeeList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNotifications() {
        String policy = policyComboBox.getValue();
        long monthsRequired = "Yearly".equals(policy) ? 12 : 6;

        List<String> employeesDue = new ArrayList<>();

        for (Employee emp : employeeList) {
            if (emp.getJoinDate() == null) continue;

            long monthsSinceJoined = ChronoUnit.MONTHS.between(emp.getJoinDate(), LocalDate.now());

            if (monthsSinceJoined >= monthsRequired) {
                employeesDue.add(emp.getName() + " (Joined on " + emp.getJoinDate() + ")");
            }
        }

        showNotificationAlert(employeesDue);
    }

    private void showNotificationAlert(List<String> employeesDue) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Salary Increment Notifications");

        if (employeesDue.isEmpty()) {
            alert.setHeaderText("No employees are due for a salary increment.");
            alert.setContentText("Checked based on the selected policy.");
        } else {
            alert.setHeaderText("The following employees are due for a salary increment:");
            alert.getDialogPane().setContent(new ScrollPane(new Label(String.join("\n", employeesDue))));
        }

        alert.showAndWait();
    }
}
