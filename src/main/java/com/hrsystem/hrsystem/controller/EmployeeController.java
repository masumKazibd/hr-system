package com.hrsystem.hrsystem.controller;

import com.hrsystem.hrsystem.model.Employee;
import com.hrsystem.hrsystem.util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeController {

    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, Integer> idColumn;
    @FXML
    private TableColumn<Employee, String> nameColumn;
    @FXML
    private TableColumn<Employee, String> departmentColumn;
    @FXML
    private TableColumn<Employee, Double> salaryColumn;

    @FXML
    private TextField nameField;
    @FXML
    private TextField departmentField;
    @FXML
    private TextField salaryField;

    @FXML
    private Button addButton; // fx:id for the Add button

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private Employee selectedEmployee = null; // To store the currently selected employee

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        loadEmployeeData();

        // Add a listener to handle table selection changes
        employeeTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showEmployeeDetails(newValue));
    }

    private void loadEmployeeData() {
        employeeList.clear();
        String query = "SELECT * FROM employees ORDER BY id";
        try (Connection conn = Database.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                employeeList.add(new Employee(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("department"),
                        resultSet.getDouble("salary")
                ));
            }
            employeeTable.setItems(employeeList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fills the text fields with the data from the selected employee.
     * @param employee The employee selected from the table.
     */
    private void showEmployeeDetails(Employee employee) {
        selectedEmployee = employee;
        if (employee != null) {
            nameField.setText(employee.getName());
            departmentField.setText(employee.getDepartment());
            salaryField.setText(String.valueOf(employee.getSalary()));
            addButton.setDisable(true); // Disable Add button when an employee is selected
        } else {
            clearFields();
        }
    }

    @FXML
    private void handleAddEmployee() {
        if (!validateInput()) return;

        String sql = "INSERT INTO employees(name, department, salary) VALUES(?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, departmentField.getText());
            pstmt.setDouble(3, Double.parseDouble(salaryField.getText()));
            pstmt.executeUpdate();

            loadEmployeeData();
            clearFields();
        } catch (SQLException | NumberFormatException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleUpdateEmployee() {
        if (selectedEmployee == null) {
            System.out.println("No employee selected. Please select an employee from the table to update.");
            return;
        }
        if (!validateInput()) return;

        String sql = "UPDATE employees SET name = ?, department = ?, salary = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, departmentField.getText());
            pstmt.setDouble(3, Double.parseDouble(salaryField.getText()));
            pstmt.setInt(4, selectedEmployee.getId());
            pstmt.executeUpdate();

            loadEmployeeData();
            clearFields();
        } catch (SQLException | NumberFormatException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleDeleteEmployee() {
        if (selectedEmployee == null) {
            System.out.println("No employee selected. Please select an employee from the table to delete.");
            return;
        }

        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedEmployee.getId());
            pstmt.executeUpdate();

            loadEmployeeData();
            clearFields();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Validates that the input fields are not empty.
     * @return true if input is valid, false otherwise.
     */
    private boolean validateInput() {
        if (nameField.getText().isEmpty() || departmentField.getText().isEmpty() || salaryField.getText().isEmpty()) {
            System.out.println("All fields are required.");
            return false;
        }
        return true;
    }

    /**
     * Clears the input text fields and the table selection.
     */
    @FXML
    public void clearFields() {
        nameField.clear();
        departmentField.clear();
        salaryField.clear();
        employeeTable.getSelectionModel().clearSelection();
        selectedEmployee = null;
        addButton.setDisable(false); // Re-enable Add button
    }
}
