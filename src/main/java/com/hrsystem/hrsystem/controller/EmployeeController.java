package com.hrsystem.hrsystem.controller;

import com.hrsystem.hrsystem.model.Department;
import com.hrsystem.hrsystem.model.Employee;
import com.hrsystem.hrsystem.util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class EmployeeController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> idColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> departmentColumn;
    @FXML private TableColumn<Employee, Double> salaryColumn;
    @FXML private TextField nameField;
    @FXML private ComboBox<Department> departmentComboBox;
    @FXML private TextField salaryField;
    @FXML private DatePicker joinDatePicker;
    @FXML private ComboBox<String> policyComboBox; // New UI element
    @FXML private Button addButton;

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private ObservableList<Department> departmentList = FXCollections.observableArrayList();
    private Employee selectedEmployee = null;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));

        loadDepartmentData();
        departmentComboBox.setItems(departmentList);

        policyComboBox.setItems(FXCollections.observableArrayList("Yearly", "Half-Yearly"));

        loadEmployeeData();

        employeeTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> showEmployeeDetails(newVal));
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
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getDouble("salary"),
                        joinLocalDate,
                        rs.getString("increment_policy")
                ));
            }
            employeeTable.setItems(employeeList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEmployeeDetails(Employee employee) {
        selectedEmployee = employee;
        if (employee != null) {
            nameField.setText(employee.getName());
            salaryField.setText(String.valueOf(employee.getSalary()));
            joinDatePicker.setValue(employee.getJoinDate());
            policyComboBox.setValue(employee.getIncrementPolicy()); // Set policy
            for (Department d : departmentComboBox.getItems()) {
                if (d.getName().equals(employee.getDepartment())) {
                    departmentComboBox.setValue(d);
                    break;
                }
            }
            addButton.setDisable(true);
        } else {
            clearFields();
        }
    }

    @FXML
    private void handleAddEmployee() {
        if (!validateInput()) return;

        String sql = "INSERT INTO employees(name, department, salary, join_date, increment_policy) VALUES(?,?,?,?,?)";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, departmentComboBox.getValue().getName());
            pstmt.setDouble(3, Double.parseDouble(salaryField.getText()));
            pstmt.setDate(4, Date.valueOf(joinDatePicker.getValue()));
            pstmt.setString(5, policyComboBox.getValue());
            pstmt.executeUpdate();

            loadEmployeeData();
            clearFields();
        } catch (SQLException | NumberFormatException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleUpdateEmployee() {
        if (selectedEmployee == null || !validateInput()) return;

        String sql = "UPDATE employees SET name = ?, department = ?, salary = ?, join_date = ?, increment_policy = ? WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, departmentComboBox.getValue().getName());
            pstmt.setDouble(3, Double.parseDouble(salaryField.getText()));
            pstmt.setDate(4, Date.valueOf(joinDatePicker.getValue()));
            pstmt.setString(5, policyComboBox.getValue());
            pstmt.setInt(6, selectedEmployee.getId());
            pstmt.executeUpdate();

            loadEmployeeData();
            clearFields();
        } catch (SQLException | NumberFormatException e) { e.printStackTrace(); }
    }

    private boolean validateInput() {
        if (nameField.getText().isEmpty() || departmentComboBox.getValue() == null || salaryField.getText().isEmpty() || joinDatePicker.getValue() == null || policyComboBox.getValue() == null) {
            System.out.println("All fields are required.");
            return false;
        }
        return true;
    }

    @FXML
    public void clearFields() {
        nameField.clear();
        departmentComboBox.setValue(null);
        salaryField.clear();
        joinDatePicker.setValue(null);
        policyComboBox.setValue(null); // Clear policy
        employeeTable.getSelectionModel().clearSelection();
        selectedEmployee = null;
        addButton.setDisable(false);
    }

    // --- Other methods (loadDepartmentData, handleDelete) remain the same ---
    private void loadDepartmentData() {
        departmentList.clear();
        String query = "SELECT * FROM departments ORDER BY name";
        try (Connection conn = Database.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                departmentList.add(new Department(
                        resultSet.getInt("id"),
                        resultSet.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteEmployee() {
        if (selectedEmployee == null) return;

        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedEmployee.getId());
            pstmt.executeUpdate();

            loadEmployeeData();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
