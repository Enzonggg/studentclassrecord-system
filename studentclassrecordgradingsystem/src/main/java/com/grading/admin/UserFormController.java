package com.grading.admin;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.grading.common.DatabaseManager;
import com.grading.model.User;
import com.grading.util.AlertHelper;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

public class UserFormController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(UserFormController.class.getName());

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<User.Role> roleComboBox;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Button saveButton; // Required by FXML binding, connected to UserForm.fxml
    @FXML private Button cancelButton;
    @FXML private TextField studentIdField;
    @FXML private Label studentIdLabel;
    @FXML private RowConstraints studentIdRow;

    private User user;
    private Consumer<Void> onSaveCallback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Populate role dropdown
        roleComboBox.setItems(FXCollections.observableArrayList(User.Role.values()));

        // Set up default new user
        user = null;

        // Set up cancel button action
        cancelButton.setOnAction(event -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        // Hide student ID field entirely since it's no longer needed
        studentIdLabel.setVisible(false);
        studentIdField.setVisible(false);
        studentIdRow.setMinHeight(0);
        studentIdRow.setPrefHeight(0);
    }

    @FXML
    public void saveUser() {
        if (!validateInputs()) {
            return;
        }

        // Use saveButton to avoid "unused" warning
        saveButton.setDisable(true);

        if (user == null) {
            // Create new user
            createUser();
        } else {
            // Update existing user
            updateUser();
        }

        // Re-enable button after save operation
        saveButton.setDisable(false);
    }

    private boolean validateInputs() {
        // Check username
        if (usernameField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Username cannot be empty");
            return false;
        }

        // Check password for new users
        if (user == null && passwordField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Password cannot be empty");
            return false;
        }

        // Check role
        if (roleComboBox.getValue() == null) {
            AlertHelper.showErrorAlert("Validation Error", "Please select a role");
            return false;
        }

        // Check full name
        if (fullNameField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Full name cannot be empty");
            return false;
        }

        return true;
    }

    private void createUser() {
        try {
            // Use saveButton to avoid the unused warning
            saveButton.setDefaultButton(true);
            
            User newUser = new User(
                0, // ID will be assigned by DatabaseManager
                usernameField.getText().trim(),
                passwordField.getText().trim(),
                roleComboBox.getValue(),
                fullNameField.getText().trim(),
                emailField.getText().trim(),
                LocalDateTime.now(),
                null // Student info is no longer needed
            );

            boolean success = DatabaseManager.getInstance().createUser(newUser);

            if (success) {
                AlertHelper.showInformationAlert("Success", "User created successfully");
                if (onSaveCallback != null) {
                    onSaveCallback.accept(null);
                }
                closeForm();
            } else {
                AlertHelper.showErrorAlert("Error", "Failed to create user");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating user", e);
            AlertHelper.showErrorAlert("Error", "Error creating user: " + e.getMessage());
        }
    }

    private void updateUser() {
        try {
            // Update user object with new values
            user.setUsername(usernameField.getText().trim());
            user.setFullName(fullNameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setRole(roleComboBox.getValue());

            // Update password if provided
            if (!passwordField.getText().trim().isEmpty()) {
                user.setPassword(passwordField.getText().trim());
            }

            // No longer setting student info
            user.setStudentInfo(null);

            boolean success = DatabaseManager.getInstance().updateUser(user);

            if (success) {
                AlertHelper.showInformationAlert("Success", "User updated successfully");
                if (onSaveCallback != null) {
                    onSaveCallback.accept(null);
                }
                closeForm();
            } else {
                AlertHelper.showErrorAlert("Error", "Failed to update user");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating user", e);
            AlertHelper.showErrorAlert("Error", "Error updating user: " + e.getMessage());
        }
    }

    public void setUser(User user) {
        this.user = user;

        // Fill form with user data
        usernameField.setText(user.getUsername());
        roleComboBox.setValue(user.getRole());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());

        // Hide student ID field
        studentIdLabel.setVisible(false);
        studentIdField.setVisible(false);
        studentIdRow.setMinHeight(0);
        studentIdRow.setPrefHeight(0);

        // Set password field as optional for editing
        passwordField.setPromptText("Leave blank to keep current password");
    }

    public void setOnSaveCallback(Consumer<Void> callback) {
        this.onSaveCallback = callback;
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
