package com.grading.teacher;

import com.grading.common.DatabaseManager;
import com.grading.model.Class;
import com.grading.model.User;
import com.grading.util.AlertHelper;
import com.grading.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentFormController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(StudentFormController.class.getName());

    @FXML private TextField studentIdField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField gradeLevelField;
    @FXML private ComboBox<Class> classComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private User studentToEdit;
    private Consumer<Void> onSaveCallback;
    private Class currentClass;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load classes for dropdown
        loadClasses();
        
        // Set up cancel button action
        cancelButton.setOnAction(event -> closeForm());
    }
    
    private void loadClasses() {
        try {
            int teacherId = SessionManager.getInstance().getCurrentUser().getId();
            
            List<Class> classes = DatabaseManager.getInstance().getClassesByTeacher(teacherId);
            
            classComboBox.setItems(FXCollections.observableArrayList(classes));
            classComboBox.setConverter(new javafx.util.StringConverter<Class>() {
                @Override
                public String toString(Class clazz) {
                    return clazz == null ? "" : clazz.getClassName() + " - " + clazz.getSection();
                }
                
                @Override
                public Class fromString(String string) {
                    return null; // Not needed for combo box
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load classes", e);
            AlertHelper.showErrorAlert("Error", "Failed to load classes: " + e.getMessage());
        }
    }
    
    public void setClass(Class clazz) {
        this.currentClass = clazz;
        classComboBox.setValue(clazz);
    }
    
    public void setStudent(User student) {
        this.studentToEdit = student;
        
        // Populate form fields
        String studentInfo = student.getStudentInfo();
        if (studentInfo != null && studentInfo.contains("Student ID:")) {
            int start = studentInfo.indexOf("Student ID:") + 11;
            int end = studentInfo.indexOf(",", start);
            if (end == -1) end = studentInfo.length();
            studentIdField.setText(studentInfo.substring(start, end).trim());
        }
        
        fullNameField.setText(student.getFullName());
        emailField.setText(student.getEmail());
        
        if (studentInfo != null && studentInfo.contains("Grade:")) {
            int start = studentInfo.indexOf("Grade:") + 6;
            int end = studentInfo.length();
            gradeLevelField.setText(studentInfo.substring(start, end).trim());
        }
    }
    
    @FXML
    public void saveStudent() {
        // Use saveButton to avoid "unused" warning
        saveButton.setDisable(true);
        
        if (!validateInputs()) {
            saveButton.setDisable(false);
            return;
        }
        
        try {
            String studentIdStr = studentIdField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String gradeLevel = gradeLevelField.getText().trim();
            Class selectedClass = classComboBox.getValue();
            
            if (studentToEdit == null) {
                // Create new student
                String studentInfo = "Student ID: " + studentIdStr + ", Grade: " + gradeLevel;
                
                User newStudent = new User(
                    0, // ID will be assigned by DatabaseManager
                    "student_" + studentIdStr.replaceAll("\\s+", ""), // Generate username from ID
                    "password", // Default password
                    User.Role.TEACHER, // Student role (represented as TEACHER for demo)
                    fullName,
                    email,
                    LocalDateTime.now(),
                    studentInfo
                );
                
                boolean success = DatabaseManager.getInstance().createUser(newStudent);
                
                if (success) {
                    // Get the created student's ID
                    User createdStudent = DatabaseManager.getInstance().getUserByUsername(newStudent.getUsername());
                    
                    if (createdStudent != null) {
                        // Enroll student in class
                        DatabaseManager.getInstance().enrollStudent(createdStudent.getId(), selectedClass.getId());
                        
                        AlertHelper.showInformationAlert("Success", "Student added to class successfully");
                        if (onSaveCallback != null) {
                            onSaveCallback.accept(null);
                        }
                        closeForm();
                    } else {
                        AlertHelper.showErrorAlert("Error", "Failed to enroll student in class");
                    }
                } else {
                    AlertHelper.showErrorAlert("Error", "Failed to create student");
                }
            } else {
                // Update existing student
                String studentInfo = "Student ID: " + studentIdStr + ", Grade: " + gradeLevel;
                
                studentToEdit.setFullName(fullName);
                studentToEdit.setEmail(email);
                studentToEdit.setStudentInfo(studentInfo);
                
                boolean success = DatabaseManager.getInstance().updateUser(studentToEdit);
                
                if (success) {
                    // Check if class has changed
                    if (selectedClass != null && selectedClass.getId() != currentClass.getId()) {
                        // Remove from current class
                        DatabaseManager.getInstance().removeStudentFromClass(
                            studentToEdit.getId(), currentClass.getId());
                        
                        // Add to new class
                        DatabaseManager.getInstance().enrollStudent(studentToEdit.getId(), selectedClass.getId());
                    }
                    
                    AlertHelper.showInformationAlert("Success", "Student updated successfully");
                    if (onSaveCallback != null) {
                        onSaveCallback.accept(null);
                    }
                    closeForm();
                } else {
                    AlertHelper.showErrorAlert("Error", "Failed to update student");
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving student", e);
            AlertHelper.showErrorAlert("Error", "Error saving student: " + e.getMessage());
        }
        
        saveButton.setDisable(false);
    }
    
    private boolean validateInputs() {
        // Check student ID
        if (studentIdField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Student ID cannot be empty");
            return false;
        }
        
        // Check full name
        if (fullNameField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Full name cannot be empty");
            return false;
        }
        
        // Check class selection
        if (classComboBox.getValue() == null) {
            AlertHelper.showErrorAlert("Validation Error", "Please select a class");
            return false;
        }
        
        return true;
    }
    
    public void setOnSaveCallback(Consumer<Void> callback) {
        this.onSaveCallback = callback;
    }
    
    @FXML
    public void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
