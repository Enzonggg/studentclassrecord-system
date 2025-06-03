package com.grading.teacher;

import com.grading.common.DatabaseManager;
import com.grading.model.Class;
import com.grading.model.User;
import com.grading.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassViewController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ClassViewController.class.getName());

    @FXML private Label classNameLabel;
    @FXML private Label sectionLabel;
    @FXML private Label subjectLabel;
    @FXML private Label studentCountLabel;
    
    @FXML private TableView<StudentInfo> studentsTable;
    @FXML private TableColumn<StudentInfo, String> studentIdColumn;
    @FXML private TableColumn<StudentInfo, String> studentNameColumn;
    @FXML private TableColumn<StudentInfo, String> studentEmailColumn;
    
    private Class currentClass;
    private final ObservableList<StudentInfo> studentsList = FXCollections.observableArrayList();
    
    // Inner class to hold student info
    public static class StudentInfo {
        private final int id;
        private final String fullName;
        private final String email;
        
        public StudentInfo(int id, String fullName, String email) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
        }
        
        public int getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        studentIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        studentNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFullName()));
        studentEmailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
    }
    
    public void setClass(Class clazz) {
        this.currentClass = clazz;
        
        // Set class details
        classNameLabel.setText(clazz.getClassName());
        sectionLabel.setText(clazz.getSection());
        subjectLabel.setText(clazz.getSubject());
        
        // Load students
        loadStudents();
    }
    
    private void loadStudents() {
        try {
            // Get the number of students
            int studentCount = DatabaseManager.getInstance().getStudentCount(currentClass.getId());
            studentCountLabel.setText(String.valueOf(studentCount));
            
            // Get list of student IDs enrolled in this class
            List<Integer> studentIds = DatabaseManager.getInstance().getStudentIdsForClass(currentClass.getId());
            
            // Get student details and create StudentInfo objects
            studentsList.clear();
            for (Integer studentId : studentIds) {
                User student = DatabaseManager.getInstance().getUserById(studentId);
                if (student != null) {
                    studentsList.add(new StudentInfo(
                        student.getId(),
                        student.getFullName(),
                        student.getEmail()
                    ));
                }
            }
            
            studentsTable.setItems(studentsList);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load students", e);
            AlertHelper.showErrorAlert("Error", "Failed to load students: " + e.getMessage());
        }
    }
    
    // This method is used by FXML, ignore "unused" warning
    @FXML
    public void closeWindow() {
        Stage stage = (Stage) classNameLabel.getScene().getWindow();
        stage.close();
    }
}
