package com.grading.teacher;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.grading.common.DatabaseManager;
import com.grading.model.Assignment;
import com.grading.model.Grade;
import com.grading.model.User;
import com.grading.util.AlertHelper;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class GradeFormController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(GradeFormController.class.getName());

    @FXML private Label studentNameLabel;
    @FXML private Label assignmentLabel;
    @FXML private TextField scoreField;
    @FXML private ComboBox<String> scoreTypeComboBox;
    @FXML private Label passFailLabel;
    @FXML private TextArea commentsArea;
    @FXML private Label percentageLabel;
    
    private int studentId;
    private Assignment assignment;
    private Grade existingGrade;
    private Consumer<Void> onSaveCallback;
    
    // Define passing threshold (can be made configurable later)
    private static final double PASSING_THRESHOLD = 60.0; // 60% is passing
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize score type dropdown
        scoreTypeComboBox.setItems(FXCollections.observableArrayList(
            "Quiz", "Exam", "Assignment", "Project", "Recitation", "Laboratory"
        ));
        scoreTypeComboBox.getSelectionModel().select(0);
        
        // Add listener to update percentage as score is entered
        scoreField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePercentageAndPassStatus();
        });
    }

    private void updatePercentageAndPassStatus() {
        try {
            if (assignment != null && !scoreField.getText().trim().isEmpty()) {
                double score = Double.parseDouble(scoreField.getText().trim());
                double percentage = (score / assignment.getMaxScore()) * 100;
                
                // Update percentage label
                percentageLabel.setText(String.format("Percentage: %.1f%% (%.1f/%.1f)", 
                    percentage, score, assignment.getMaxScore()));
                
                // Update pass/fail status
                if (percentage >= PASSING_THRESHOLD) {
                    passFailLabel.setText("PASSED");
                    passFailLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else {
                    passFailLabel.setText("FAILED");
                    passFailLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            } else {
                percentageLabel.setText("Percentage: 0%");
                passFailLabel.setText("Not calculated");
                passFailLabel.setStyle("");
            }
        } catch (NumberFormatException e) {
            percentageLabel.setText("Invalid score format");
            passFailLabel.setText("Not calculated");
            passFailLabel.setStyle("");
        }
    }
    
    public void setStudentAndAssignment(int studentId, Assignment assignment) {
        this.studentId = studentId;
        this.assignment = assignment;
        
        try {
            // Load student name
            User student = DatabaseManager.getInstance().getUserById(studentId);
            if (student != null) {
                studentNameLabel.setText(student.getFullName());
                LOGGER.info(() -> "Student loaded: " + student.getFullName());
            }
            
            // Set assignment info
            assignmentLabel.setText(assignment.getTitle() + " (Max Score: " + 
                assignment.getMaxScore() + ")");
            LOGGER.info(() -> "Assignment loaded: " + assignment.getTitle());
            
            // Set score type based on assignment type
            switch (assignment.getType()) {
                case QUIZ:
                    scoreTypeComboBox.setValue("Quiz");
                    break;
                case EXAM:
                    scoreTypeComboBox.setValue("Exam");
                    break;
                case ASSIGNMENT:
                    scoreTypeComboBox.setValue("Assignment");
                    break;
                case PROJECT:
                    scoreTypeComboBox.setValue("Project");
                    break;
                default:
                    scoreTypeComboBox.setValue("Quiz");
            }
            
            // Check for existing grade
            existingGrade = DatabaseManager.getInstance().getGradeByStudentAndAssignment(
                studentId, assignment.getId());
            
            if (existingGrade != null) {
                scoreField.setText(String.valueOf(existingGrade.getScore()));
                commentsArea.setText(existingGrade.getComments());
                updatePercentageAndPassStatus();
                LOGGER.info(() -> "Existing grade loaded: " + existingGrade.getScore());
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading student or assignment data", e);
        }
    }
    
    @FXML
    public void saveGrade() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            double score = Double.parseDouble(scoreField.getText().trim());
            String comments = commentsArea.getText().trim();
            String scoreType = scoreTypeComboBox.getValue();
            
            // Add score type to comments if not already there
            if (!comments.contains("Type: ")) {
                comments = "Type: " + scoreType + "\n" + comments;
            }
            
            // Calculate if passed or failed
            double percentage = (score / assignment.getMaxScore()) * 100;
            String passStatus = percentage >= PASSING_THRESHOLD ? "PASSED" : "FAILED";
            
            // Add pass status to comments
            if (!comments.contains("Status: ")) {
                comments += "\nStatus: " + passStatus + " (" + String.format("%.1f%%", percentage) + ")";
            }
            
            Grade grade;
            if (existingGrade != null) {
                grade = existingGrade;
                grade.setScore(score);
                grade.setComments(comments);
                grade.setGradedAt(LocalDateTime.now());
            } else {
                grade = new Grade(
                    0, // ID will be assigned by DatabaseManager
                    studentId,
                    assignment.getId(),
                    score,
                    comments,
                    LocalDateTime.now()
                );
            }
            
            // Set additional properties for UI display
            grade.setAssignmentTitle(assignment.getTitle());
            grade.setMaxScore(assignment.getMaxScore());
            grade.setClassId(assignment.getClassId());
            
            boolean success = DatabaseManager.getInstance().createGrade(grade);
            
            if (success) {
                AlertHelper.showInformationAlert("Success", "Grade saved successfully");
                if (onSaveCallback != null) {
                    onSaveCallback.accept(null);
                }
                closeForm();
            } else {
                AlertHelper.showErrorAlert("Error", "Failed to save grade");
            }
            
        } catch (NumberFormatException e) {
            AlertHelper.showErrorAlert("Invalid Input", "Please enter a valid number for the score");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving grade", e);
            AlertHelper.showErrorAlert("Error", "Error saving grade: " + e.getMessage());
        }
    }
    
    private boolean validateInputs() {
        try {
            double score = Double.parseDouble(scoreField.getText().trim());
            if (score < 0 || score > assignment.getMaxScore()) {
                AlertHelper.showErrorAlert("Invalid Score", 
                    "Score must be between 0 and " + assignment.getMaxScore());
                return false;
            }
            
            if (scoreTypeComboBox.getValue() == null) {
                AlertHelper.showErrorAlert("Missing Information", "Please select a score type");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showErrorAlert("Invalid Input", "Please enter a valid number for the score");
            return false;
        }
        
        return true;
    }
    
    @FXML
    public void closeForm() {
        Stage stage = (Stage) scoreField.getScene().getWindow();
        stage.close();
    }
    
    public void setOnSaveCallback(Consumer<Void> callback) {
        this.onSaveCallback = callback;
    }
}
