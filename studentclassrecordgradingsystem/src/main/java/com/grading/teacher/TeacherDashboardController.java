package com.grading.teacher;

import com.grading.common.DatabaseManager;
import com.grading.model.Class;
import com.grading.model.Grade;
import com.grading.model.Assignment;
import com.grading.model.User;
import com.grading.util.AlertHelper;
import com.grading.util.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeacherDashboardController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(TeacherDashboardController.class.getName());

    @FXML private Label totalClassesLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label assignmentsDueLabel;
    @FXML private Label userLabel;
    
    @FXML private TableView<Class> recentClassesTable;
    @FXML private TableColumn<Class, String> classNameColumn;
    @FXML private TableColumn<Class, String> subjectColumn;
    @FXML private TableColumn<Class, String> sectionColumn;
    @FXML private TableColumn<Class, Integer> studentsColumn;
    @FXML private TableColumn<Class, Void> actionsColumn;
    
    @FXML private TableView<Map<String, Object>> gradesTable; // Changed from TableView<?> to correct type
    @FXML private ComboBox<Class> classSelectionComboBox;
    
    @FXML private TabPane tabPane;
    @FXML private Tab dashboardTab;
    @FXML private Tab gradesTab;
    
    private final ObservableList<Class> classesList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set user info
        if (SessionManager.getInstance().getCurrentUser() != null) {
            userLabel.setText("Logged in as: " + SessionManager.getInstance().getCurrentUser().getFullName());
        }
        
        // Initialize table columns
        classNameColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        sectionColumn.setCellValueFactory(new PropertyValueFactory<>("section"));
        studentsColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleIntegerProperty(getStudentCount(cellData.getValue().getId())).asObject();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error getting student count", e);
                return new SimpleIntegerProperty(0).asObject();
            }
        });
        
        // Setup action buttons
        setupActionsColumn();
        
        // Load dashboard data
        loadDashboardData();
        
        // Load classes for the teacher
        loadClasses();
        
        // Setup class selection combo box
        classSelectionComboBox.setItems(classesList);
        classSelectionComboBox.setConverter(new javafx.util.StringConverter<Class>() {
            @Override
            public String toString(Class clazz) {
                return clazz == null ? "" : clazz.getClassName() + " - " + clazz.getSection();
            }
            
            @Override
            public Class fromString(String string) {
                return null; // Not needed for combo box
            }
        });
        
        classSelectionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadGradesForClass(newVal.getId());
            }
        });
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<Class, Void>, TableCell<Class, Void>> cellFactory = param -> 
            new TableCell<>() {
                private final Button viewBtn = new Button("View");
                private final Button gradesBtn = new Button("Grades");
                private final HBox buttons = new HBox(5, viewBtn, gradesBtn);
                
                {
                    viewBtn.getStyleClass().add("secondary-button");
                    gradesBtn.getStyleClass().add("secondary-button");
                    
                    viewBtn.setOnAction(event -> {
                        Class clazz = getTableView().getItems().get(getIndex());
                        viewClass(clazz);
                    });
                    
                    gradesBtn.setOnAction(event -> {
                        Class clazz = getTableView().getItems().get(getIndex());
                        showGradesForClass(clazz);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttons);
                    }
                }
            };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    private void loadDashboardData() {
        try {
            int teacherId = SessionManager.getInstance().getCurrentUser().getId();
            
            // Get classes for this teacher
            List<Class> classes = DatabaseManager.getInstance().getClassesByTeacher(teacherId);
            totalClassesLabel.setText(String.valueOf(classes.size()));
            
            // Count total students (may have duplicates across classes)
            int totalStudents = 0;
            for (Class clazz : classes) {
                totalStudents += DatabaseManager.getInstance().getStudentCount(clazz.getId());
            }
            totalStudentsLabel.setText(String.valueOf(totalStudents));
            
            // Count assignments due (placeholder for now)
            assignmentsDueLabel.setText("0");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load dashboard data", e);
            AlertHelper.showErrorAlert("Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    private void loadClasses() {
        try {
            int teacherId = SessionManager.getInstance().getCurrentUser().getId();
            
            // Get classes for this teacher
            List<Class> classes = DatabaseManager.getInstance().getClassesByTeacher(teacherId);
            
            // Debug log to see what's being loaded
            LOGGER.log(Level.INFO, "Loaded {0} classes for teacher ID {1}", 
                     new Object[]{classes.size(), teacherId});
            
            classesList.clear();
            classesList.addAll(classes);
            
            recentClassesTable.setItems(classesList);
            
            // Ensure table refresh
            recentClassesTable.refresh();
            
            // If no classes are shown, add a debug class
            if (classes.isEmpty()) {
                LOGGER.log(Level.WARNING, "No classes found for teacher ID: {0}", teacherId);
                // Create a debug class in the database if needed
                createSampleClassIfNeeded(teacherId);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load classes", e);
            AlertHelper.showErrorAlert("Error", "Failed to load classes: " + e.getMessage());
        }
    }
    
    private void createSampleClassIfNeeded(int teacherId) {
        // Check if teacher already has classes
        List<Class> existingClasses = DatabaseManager.getInstance().getClassesByTeacher(teacherId);
        if (existingClasses.isEmpty()) {
            // Create a sample class for testing
            Class sampleClass = new Class(
                0,  // ID will be assigned by DatabaseManager
                "Sample Math Class",
                "Section A",
                "Mathematics",
                teacherId,
                "2024-2025"
            );
            
            boolean success = DatabaseManager.getInstance().createClass(sampleClass);
            if (success) {
                LOGGER.log(Level.INFO, "Created sample class for teacher ID: {0}", teacherId);
                // Reload classes
                loadClasses();
            }
        }
    }
    
    private int getStudentCount(int classId) throws SQLException {
        return DatabaseManager.getInstance().getStudentCount(classId);
    }
    
    private void viewClass(Class clazz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher/ClassView.fxml"));
            Parent root = loader.load();
            
            ClassViewController controller = loader.getController();
            controller.setClass(clazz);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Class Details");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load class view", e);
            AlertHelper.showErrorAlert("Error", "Could not load class view: " + e.getMessage());
        }
    }
    
    private void showGradesForClass(Class clazz) {
        tabPane.getSelectionModel().select(gradesTab);
        classSelectionComboBox.getSelectionModel().select(clazz);
        loadGradesForClass(clazz.getId());
    }
    
    private void loadGradesForClass(int classId) {
        try {
            LOGGER.log(Level.INFO, "Loading grades for class ID: {0}", classId);
            
            // Get enrolled students for this class
            List<Integer> studentIds = DatabaseManager.getInstance().getStudentIdsForClass(classId);
            List<Assignment> assignments = DatabaseManager.getInstance().getAssignmentsByClass(classId);
            
            if (assignments.isEmpty()) {
                AlertHelper.showInformationAlert("No Assignments", 
                    "This class doesn't have any assignments yet. Please add assignments first.");
                
                // Clear the gradesTable
                gradesTable.getItems().clear();
                return;
            }
            
            // Create table columns dynamically based on assignments
            gradesTable.getColumns().clear();
            
            // Add student columns
            TableColumn<Map<String, Object>, String> studentNameColumn = new TableColumn<>("Student Name");
            studentNameColumn.setCellValueFactory(param -> 
                new SimpleStringProperty((String) param.getValue().get("studentName")));
            studentNameColumn.setPrefWidth(200);
            
            TableColumn<Map<String, Object>, String> studentIdColumn = new TableColumn<>("Student ID");
            studentIdColumn.setCellValueFactory(param -> 
                new SimpleStringProperty(param.getValue().get("studentId").toString()));
            studentIdColumn.setPrefWidth(100);
            
            gradesTable.getColumns().add(studentNameColumn);
            gradesTable.getColumns().add(studentIdColumn);
            
            // Add column for each assignment
            for (Assignment assignment : assignments) {
                TableColumn<Map<String, Object>, String> assignmentColumn = 
                    new TableColumn<>(assignment.getTitle() + " (" + assignment.getMaxScore() + ")");
                
                assignmentColumn.setCellValueFactory(param -> {
                    Object gradeValue = param.getValue().get("grade_" + assignment.getId());
                    if (gradeValue != null) {
                        double score = Double.parseDouble(gradeValue.toString());
                        // Format as score/maxScore for better readability
                        return new SimpleStringProperty(String.format("%.1f/%.1f", score, assignment.getMaxScore()));
                    }
                    return new SimpleStringProperty("N/A");
                });
                
                // Make the column editable with better UI
                assignmentColumn.setCellFactory(col -> new TableCell<>() {
                    private final Button gradeBtn = new Button("Grade");
                    
                    {
                        gradeBtn.getStyleClass().add("secondary-button");
                        gradeBtn.setOnAction(e -> {
                            Map<String, Object> rowData = getTableView().getItems().get(getIndex());
                            int studentId = (Integer) rowData.get("studentId");
                            String studentName = (String) rowData.get("studentName");
                            showGradeForm(studentId, studentName, assignment, classId);
                        });
                    }
                    
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            if (item.equals("N/A")) {
                                setGraphic(gradeBtn);
                                setText(null);
                            } else {
                                setGraphic(null);
                                setText(item);
                            }
                        }
                    }
                });
                
                assignmentColumn.setPrefWidth(120);
                gradesTable.getColumns().add(assignmentColumn);
            }
            
            // Load student data
            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
            
            for (Integer studentId : studentIds) {
                User student = DatabaseManager.getInstance().getUserById(studentId);
                if (student != null) {
                    Map<String, Object> rowData = new HashMap<>();
                    rowData.put("studentId", studentId);
                    rowData.put("studentName", student.getFullName());
                    
                    // Load existing grades for each assignment
                    for (Assignment assignment : assignments) {
                        Grade grade = DatabaseManager.getInstance().getGradeByStudentAndAssignment(
                            studentId, assignment.getId());
                        
                        if (grade != null) {
                            rowData.put("grade_" + assignment.getId(), String.valueOf(grade.getScore()));
                        }
                    }
                    
                    data.add(rowData);
                }
            }
            
            gradesTable.setItems(data);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading grades for class", e);
            AlertHelper.showErrorAlert("Error", "Failed to load grades: " + e.getMessage());
        }
    }
    
    private void showGradeForm(int studentId, String studentName, Assignment assignment, int classId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher/GradeForm.fxml"));
            Parent root = loader.load();
            
            GradeFormController controller = loader.getController();
            controller.setStudentAndAssignment(studentId, assignment);
            controller.setOnSaveCallback(unused -> loadGradesForClass(classId));
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Grade Assignment for " + studentName); // Now using the studentName parameter
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load grade form", e);
            AlertHelper.showErrorAlert("Error", "Could not load grade form: " + e.getMessage());
        }
    }
    
    @FXML
    public void showDashboard() {
        tabPane.getSelectionModel().select(dashboardTab);
        loadDashboardData();
    }
    
    @FXML
    public void showGrades() {
        tabPane.getSelectionModel().select(gradesTab);
    }
    
    @FXML
    public void showAttendance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher/Attendance.fxml"));
            Parent root = loader.load();
            
            Tab attendanceTab = new Tab("Attendance");
            attendanceTab.setContent(root);
            
            if (!tabExists("Attendance")) {
                tabPane.getTabs().add(attendanceTab);
            }
            
            tabPane.getSelectionModel().select(attendanceTab);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load attendance view", e);
            AlertHelper.showErrorAlert("Error", "Could not load attendance: " + e.getMessage());
        }
    }
    
    @FXML
    public void showReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher/TeacherReports.fxml"));
            Parent root = loader.load();
            
            Tab reportsTab = new Tab("Reports");
            reportsTab.setContent(root);
            
            if (!tabExists("Reports")) {
                tabPane.getTabs().add(reportsTab);
            }
            
            tabPane.getSelectionModel().select(reportsTab);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load reports view", e);
            AlertHelper.showErrorAlert("Error", "Could not load reports: " + e.getMessage());
        }
    }
    
    @FXML
    public void showStudentManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher/StudentManagement.fxml"));
            Parent root = loader.load();
            
            Tab studentsTab = new Tab("Student Management");
            studentsTab.setContent(root);
            
            if (!tabExists("Student Management")) {
                tabPane.getTabs().add(studentsTab);
            }
            
            tabPane.getSelectionModel().select(studentsTab);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load student management view", e);
            AlertHelper.showErrorAlert("Error", "Could not load student management: " + e.getMessage());
        }
    }
    
    private boolean tabExists(String title) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals(title)) {
                return true;
            }
        }
        return false;
    }
    
    @FXML
    public void addNewAssignment() {
        Class selectedClass = classSelectionComboBox.getValue();
        if (selectedClass == null) {
            AlertHelper.showErrorAlert("Error", "Please select a class first.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher/AssignmentForm.fxml"));
            Parent root = loader.load();
            
            AssignmentFormController controller = loader.getController();
            controller.setClass(selectedClass);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Record Class Score");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh data after adding score
            loadDashboardData();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load score entry form", e);
            AlertHelper.showErrorAlert("Error", "Could not load score entry form: " + e.getMessage());
        }
    }
    
    @FXML
    public void logout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
        
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/common/Login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load login page", e);
            AlertHelper.showErrorAlert("Error", "Could not load login page: " + e.getMessage());
        }
    }
}
