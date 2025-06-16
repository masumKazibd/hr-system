package com.hrsystem.hrsystem.controller;

import com.hrsystem.hrsystem.model.Department;
import com.hrsystem.hrsystem.util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

    private ObservableList<Department> departmentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        loadDepartmentData();
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

    @FXML
    private void handleAddDepartment() {
        String name = nameField.getText();
        if (name.isEmpty()) {
            System.out.println("Department name cannot be empty.");
            return;
        }

        String sql = "INSERT INTO departments(name) VALUES(?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.executeUpdate();

            loadDepartmentData();
            nameField.clear();
        } catch (SQLException e) {
            System.out.println("Could not add department. It might already exist.");
            e.printStackTrace();
        }
    }
}
