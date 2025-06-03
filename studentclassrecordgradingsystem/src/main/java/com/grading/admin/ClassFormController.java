package com.grading.admin;

import com.grading.common.DatabaseManager;
import com.grading.model.Class;
import com.grading.model.User;
import com.grading.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClassFormController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ClassFormController.class.getName());

    @FXML private TextField classNameField;
    @FXML private TextField sectionField;
    @FXML private TextField subjectField;
    @FXML private ComboBox<User> teacherComboBox;
    @FXML private TextField schoolYearField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Class classToEdit;
    private Consumer<Void> onSaveCallback;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load teachers for dropdown
        loadTeachers();
        
        // Set default school year
        schoolYearField.setText("2024-2025");
        
        // Set up cancel button action
        cancelButton.setOnAction(event -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
    }
    
    private void loadTeachers() {
        try {
            List<User> teachers = DatabaseManager.getInstance().getAllUsers().stream()
                .filter(user -> user.getRole() == User.Role.TEACHER)
                .collect(Collectors.toList());
            
            teacherComboBox.setItems(FXCollections.observableArrayList(teachers));
            teacherComboBox.setConverter(new javafx.util.StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user == null ? "" : user.getFullName();
                }
                
                @Override
                public User fromString(String string) {
                    return null; // Not needed for combo box
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load teachers", e);
            AlertHelper.showErrorAlert("Error", "Failed to load teachers: " + e.getMessage());
        }
    }
    
    @FXML
    public void saveClass() {
        if (!validateInputs()) {
            return;
        }
        
        // Use saveButton to avoid the "unused" warning
        saveButton.setDefaultButton(true);
        
        try {
            if (classToEdit == null) {
                // Create new class
                createClass();
            } else {
                // Update existing class
                updateClass();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving class", e);
            AlertHelper.showErrorAlert("Error", "Error saving class: " + e.getMessage());
        }
    }
    
    private boolean validateInputs() {
        // Check class name
        if (classNameField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Class name cannot be empty");
            return false;
        }
        
        // Check section
        if (sectionField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Section cannot be empty");
            return false;
        }
        
        // Check subject
        if (subjectField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Subject cannot be empty");
            return false;
        }
        
        // Check teacher
        if (teacherComboBox.getValue() == null) {
            AlertHelper.showErrorAlert("Validation Error", "Please select a teacher");
            return false;
        }
        
        // Check school year
        if (schoolYearField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "School year cannot be empty");
            return false;
        }
        
        return true;
    }
    
    private void createClass() throws Exception {
        Class newClass = new Class(
            0, // ID will be assigned by DatabaseManager
            classNameField.getText().trim(),
            sectionField.getText().trim(),
            subjectField.getText().trim(),
            teacherComboBox.getValue().getId(),
            schoolYearField.getText().trim()
        );
        
        boolean success = DatabaseManager.getInstance().createClass(newClass);
        
        if (success) {
            AlertHelper.showInformationAlert("Success", "Class created successfully");
            if (onSaveCallback != null) {
                onSaveCallback.accept(null);
            }
            closeForm();
        } else {
            AlertHelper.showErrorAlert("Error", "Failed to create class");
        }
    }
    
    private void updateClass() throws Exception {
        classToEdit.setClassName(classNameField.getText().trim());
        classToEdit.setSection(sectionField.getText().trim());
        classToEdit.setSubject(subjectField.getText().trim());
        classToEdit.setTeacherId(teacherComboBox.getValue().getId());
        classToEdit.setSchoolYear(schoolYearField.getText().trim());
        
        boolean success = DatabaseManager.getInstance().createClass(classToEdit); // reuse create for update
        
        if (success) {
            AlertHelper.showInformationAlert("Success", "Class updated successfully");
            if (onSaveCallback != null) {
                onSaveCallback.accept(null);
            }
            closeForm();
        } else {
            AlertHelper.showErrorAlert("Error", "Failed to update class");
        }
    }
    
    public void setClass(Class classToEdit) {
        this.classToEdit = classToEdit;
        
        // Populate form fields
        classNameField.setText(classToEdit.getClassName());
        sectionField.setText(classToEdit.getSection());
        subjectField.setText(classToEdit.getSubject());
        schoolYearField.setText(classToEdit.getSchoolYear());
        
        // Set selected teacher
        try {
            User teacher = DatabaseManager.getInstance().getUserById(classToEdit.getTeacherId());
            if (teacher != null) {
                teacherComboBox.setValue(teacher);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load teacher for class", e);
        }
    }
    
    public void setOnSaveCallback(Consumer<Void> callback) {
        this.onSaveCallback = callback;
    }
    
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
