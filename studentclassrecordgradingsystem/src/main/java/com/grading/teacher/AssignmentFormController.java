package com.grading.teacher;

import com.grading.common.DatabaseManager;
import com.grading.model.Assignment;
import com.grading.model.Class;
import com.grading.model.Grade;
import com.grading.model.User;
import com.grading.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssignmentFormController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(AssignmentFormController.class.getName());

    @FXML private Label classNameLabel;
    @FXML private ComboBox<User> studentComboBox;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField scoreField;
    @FXML private TextField maxScoreField;
    @FXML private TextField dateField;
    
    private Class currentClass;
    private Consumer<Void> onSaveCallback;
    private final Map<String, User> studentMap = new HashMap<>();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize type dropdown with simple assessment types
        typeComboBox.setItems(FXCollections.observableArrayList(
            "Quiz", "Exam", "Assignment", "Project", "Recitation", "Laboratory"
        ));
        typeComboBox.getSelectionModel().select(0);
        
        // Set default date to today
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        
        // Set default max score
        maxScoreField.setText("100");
    }
    
    public void setClass(Class clazz) {
        this.currentClass = clazz;
        classNameLabel.setText(clazz.getClassName() + " - " + clazz.getSection());
        
        // Load students in this class
        loadStudents();
    }
    
    private void loadStudents() {
        try {
            // Get students enrolled in this class
            List<Integer> studentIds = DatabaseManager.getInstance().getStudentIdsForClass(currentClass.getId());
            
            // Clear previous data
            studentComboBox.getItems().clear();
            studentMap.clear();
            
            // Check if any students are enrolled
            if (studentIds.isEmpty()) {
                AlertHelper.showWarningAlert("No Students", 
                    "There are no students enrolled in this class.\n" +
                    "Please add students to the class first.");
                return;
            }
            
            // Load student details
            for (Integer studentId : studentIds) {
                User student = DatabaseManager.getInstance().getUserById(studentId);
                if (student != null) {
                    studentComboBox.getItems().add(student);
                    studentMap.put(student.getFullName(), student);
                }
            }
            
            // Set up the display for the combo box
            studentComboBox.setConverter(new javafx.util.StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return user == null ? "" : user.getFullName();
                }
                
                @Override
                public User fromString(String string) {
                    return studentMap.get(string);
                }
            });
            
            // Select first student by default
            if (!studentComboBox.getItems().isEmpty()) {
                studentComboBox.getSelectionModel().select(0);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading students", e);
            AlertHelper.showErrorAlert("Error", "Failed to load students: " + e.getMessage());
        }
    }
    
    @FXML
    public void saveScore() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            User selectedStudent = studentComboBox.getValue();
            String title = titleField.getText().trim();
            String type = typeComboBox.getValue();
            double score = Double.parseDouble(scoreField.getText().trim());
            double maxScore = Double.parseDouble(maxScoreField.getText().trim());
            
            // Create a new assignment if needed
            Assignment assignment = new Assignment(
                0, // ID will be assigned by DatabaseManager
                currentClass.getId(),
                title,
                "Score record for " + type,
                getAssignmentTypeFromString(type),
                maxScore,
                1.0, // Default weight
                LocalDate.now(), // Use today's date
                LocalDateTime.now()
            );
            
            boolean success = DatabaseManager.getInstance().createAssignment(assignment);
            
            if (success) {
                // Get the created assignment ID
                int assignmentId = assignment.getId();
                
                // Create grade record for the selected student
                Grade grade = new Grade(
                    0, // ID will be assigned by DatabaseManager
                    selectedStudent.getId(), // Use selected student ID
                    assignmentId,
                    score,
                    "Score recorded on " + LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")),
                    LocalDateTime.now()
                );
                
                success = DatabaseManager.getInstance().createGrade(grade);
                
                if (success) {
                    AlertHelper.showInformationAlert("Success", 
                        "Score recorded successfully for " + selectedStudent.getFullName() + ": " + score + "/" + maxScore);
                    if (onSaveCallback != null) {
                        onSaveCallback.accept(null);
                    }
                    closeForm();
                } else {
                    AlertHelper.showErrorAlert("Error", "Failed to record score");
                }
            } else {
                AlertHelper.showErrorAlert("Error", "Failed to create assessment record");
            }
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Error parsing numeric input", e);
            AlertHelper.showErrorAlert("Error", "Invalid number format: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording score", e);
            AlertHelper.showErrorAlert("Error", "Error recording score: " + e.getMessage());
        }
    }
    
    private Assignment.Type getAssignmentTypeFromString(String typeString) {
        switch (typeString.toLowerCase()) {
            case "quiz":
                return Assignment.Type.QUIZ;
            case "exam":
                return Assignment.Type.EXAM;
            case "project":
                return Assignment.Type.PROJECT;
            default:
                return Assignment.Type.ASSIGNMENT;
        }
    }
    
    private boolean validateInputs() {
        // Check if student is selected
        if (studentComboBox.getValue() == null) {
            AlertHelper.showErrorAlert("Validation Error", "Please select a student");
            return false;
        }
        
        // Check title
        if (titleField.getText().trim().isEmpty()) {
            AlertHelper.showErrorAlert("Validation Error", "Title cannot be empty");
            return false;
        }
        
        // Check type
        if (typeComboBox.getValue() == null) {
            AlertHelper.showErrorAlert("Validation Error", "Please select an assessment type");
            return false;
        }
        
        // Check score and max score
        try {
            double score = Double.parseDouble(scoreField.getText().trim());
            if (score < 0) {
                AlertHelper.showErrorAlert("Validation Error", "Score must be a positive number");
                return false;
            }
            
            double maxScore = Double.parseDouble(maxScoreField.getText().trim());
            if (maxScore <= 0) {
                AlertHelper.showErrorAlert("Validation Error", "Max score must be greater than 0");
                return false;
            }
            
            if (score > maxScore) {
                AlertHelper.showErrorAlert("Validation Error", "Score cannot exceed max score");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showErrorAlert("Validation Error", "Score and max score must be valid numbers");
            return false;
        }
        
        return true;
    }
    
    @FXML
    public void closeForm() {
        Stage stage = (Stage) classNameLabel.getScene().getWindow();
        stage.close();
    }
    
    public void setOnSaveCallback(Consumer<Void> callback) {
        this.onSaveCallback = callback;
    }
}
