package com.hrsystem.hrsystem.controller;

import com.hrsystem.hrsystem.model.Department;
import com.hrsystem.hrsystem.model.Employee;
import com.hrsystem.hrsystem.util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
    private ComboBox<Department> departmentComboBox;
    @FXML
    private TextField salaryField;
    @FXML
    private Button addButton;

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

        loadEmployeeData();

        employeeTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showEmployeeDetails(newValue));
    }

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

    private void showEmployeeDetails(Employee employee) {
        selectedEmployee = employee;
        if (employee != null) {
            nameField.setText(employee.getName());
            salaryField.setText(String.valueOf(employee.getSalary()));

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

        String departmentName = departmentComboBox.getValue().getName();

        String sql = "INSERT INTO employees(name, department, salary) VALUES(?,?,?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, departmentName);
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
        if (selectedEmployee == null || !validateInput()) return;

        String departmentName = departmentComboBox.getValue().getName();

        String sql = "UPDATE employees SET name = ?, department = ?, salary = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, departmentName);
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
        if (selectedEmployee == null) return;

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

    private boolean validateInput() {
        if (nameField.getText().isEmpty() || departmentComboBox.getValue() == null || salaryField.getText().isEmpty()) {
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
        employeeTable.getSelectionModel().clearSelection();
        selectedEmployee = null;
        addButton.setDisable(false);
    }
}
