package com.hrsystem.hrsystem.controller;

import com.hrsystem.hrsystem.model.Department;
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

public class DepartmentController {

    @FXML
    private TableView<Department> departmentTable;
    @FXML
    private TableColumn<Department, Integer> idColumn;
    @FXML
    private TableColumn<Department, String> nameColumn;
    @FXML
    private TextField nameField;
    @FXML
    private Button addButton;

    private ObservableList<Department> departmentList = FXCollections.observableArrayList();
    private Department selectedDepartment = null;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        loadDepartmentData();

        departmentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDepartmentDetails(newValue));
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
            departmentTable.setItems(departmentList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showDepartmentDetails(Department department) {
        selectedDepartment = department;
        if (department != null) {
            nameField.setText(department.getName());
            addButton.setDisable(true);
        } else {
            clearFields();
        }
    }

    @FXML
    private void handleAddDepartment() {
        String name = nameField.getText();
        if (name.isEmpty()) {
            System.out.println("Validation Error: Department name cannot be empty.");
            return;
        }

        String sql = "INSERT INTO departments(name) VALUES(?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.executeUpdate();

            loadDepartmentData();
            clearFields();
        } catch (SQLException e) {
            System.out.println("Database Error: Could not add department. It might already exist.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateDepartment() {
        if (selectedDepartment == null) {
            System.out.println("Selection Error: Please select a department to update.");
            return;
        }
        String name = nameField.getText();
        if (name.isEmpty()) {
            System.out.println("Validation Error: Department name cannot be empty.");
            return;
        }

        String sql = "UPDATE departments SET name = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, selectedDepartment.getId());
            pstmt.executeUpdate();

            loadDepartmentData();
            clearFields();
        } catch (SQLException e) {
            System.out.println("Database Error: Could not update department.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteDepartment() {
        if (selectedDepartment == null) {
            System.out.println("Selection Error: Please select a department to delete.");
            return;
        }

        // Check if department is in use before allowing deletion
        if (isDepartmentInUse(selectedDepartment.getName())) {
            System.out.println("Deletion Error: Cannot delete this department. It is currently assigned to one or more employees.");
            return;
        }

        String sql = "DELETE FROM departments WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedDepartment.getId());
            pstmt.executeUpdate();

            loadDepartmentData();
            clearFields();
        } catch (SQLException e) {
            System.out.println("Database Error: Could not delete department.");
            e.printStackTrace();
        }
    }

    /**
     * Checks if a department is assigned to any employee.
     * @param departmentName The name of the department to check.
     * @return true if the department is in use, false otherwise.
     */
    private boolean isDepartmentInUse(String departmentName) {
        String sql = "SELECT COUNT(*) FROM employees WHERE department = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, departmentName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    public void clearFields() {
        nameField.clear();
        departmentTable.getSelectionModel().clearSelection();
        selectedDepartment = null;
        addButton.setDisable(false);
    }
}
