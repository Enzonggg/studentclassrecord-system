package com.grading.teacher;

import com.grading.common.DatabaseManager;
import com.grading.model.Class;
import com.grading.model.User;
import com.grading.util.AlertHelper;
import com.grading.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudentManagementController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(StudentManagementController.class.getName());

    @FXML private ComboBox<Class> classSelectionComboBox;
    
    @FXML private TableView<User> studentsTable;
    @FXML private TableColumn<User, String> studentIdColumn;
    @FXML private TableColumn<User, String> studentNameColumn;
    @FXML private TableColumn<User, String> studentEmailColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    
    private final ObservableList<Class> classesList = FXCollections.observableArrayList();
    private final ObservableList<User> studentsList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup table columns
        studentIdColumn.setCellValueFactory(cellData -> {
            String studentInfo = cellData.getValue().getStudentInfo();
            if (studentInfo != null && studentInfo.contains("Student ID:")) {
                int start = studentInfo.indexOf("Student ID:") + 11;
                int end = studentInfo.indexOf(",", start);
                if (end == -1) end = studentInfo.length();
                return new SimpleStringProperty(studentInfo.substring(start, end).trim());
            }
            return new SimpleStringProperty("N/A");
        });
        
        studentNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFullName()));
            
        studentEmailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
        
        setupActionsColumn();
        loadClasses();
        
        // Setup class selection listener
        classSelectionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadStudentsForClass(newVal);
            }
        });
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> 
            new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button removeBtn = new Button("Remove");
                private final HBox buttons = new HBox(5, editBtn, removeBtn);
                
                {
                    editBtn.getStyleClass().add("secondary-button");
                    removeBtn.getStyleClass().add("secondary-button");
                    
                    editBtn.setOnAction(event -> {
                        User student = getTableView().getItems().get(getIndex());
                        editStudent(student);
                    });
                    
                    removeBtn.setOnAction(event -> {
                        User student = getTableView().getItems().get(getIndex());
                        removeStudent(student);
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
    
    private void loadClasses() {
        try {
            int teacherId = SessionManager.getInstance().getCurrentUser().getId();
            
            List<Class> classes = DatabaseManager.getInstance().getClassesByTeacher(teacherId);
            
            classesList.clear();
            classesList.addAll(classes);
            
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
            
            if (!classes.isEmpty()) {
                classSelectionComboBox.getSelectionModel().select(0);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load classes", e);
            AlertHelper.showErrorAlert("Error", "Failed to load classes: " + e.getMessage());
        }
    }
    
    private void loadStudentsForClass(Class selectedClass) {
        try {
            List<Integer> studentIds = DatabaseManager.getInstance().getStudentIdsForClass(selectedClass.getId());
            
            studentsList.clear();
            
            for (Integer studentId : studentIds) {
                User student = DatabaseManager.getInstance().getUserById(studentId);
                if (student != null) {
                    studentsList.add(student);
                }
            }
            
            studentsTable.setItems(studentsList);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load students", e);
            AlertHelper.showErrorAlert("Error", "Failed to load students: " + e.getMessage());
        }
    }
    
    @FXML
    public void addStudent() {
        Class selectedClass = classSelectionComboBox.getValue();
        if (selectedClass == null) {
            AlertHelper.showErrorAlert("Error", "Please select a class first.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher/StudentForm.fxml"));
            Parent root = loader.load();
            
            StudentFormController controller = loader.getController();
            controller.setClass(selectedClass);
            controller.setOnSaveCallback(unused -> loadStudentsForClass(selectedClass));
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Student");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load student form", e);
            AlertHelper.showErrorAlert("Error", "Could not load student form: " + e.getMessage());
        }
    }
    
    private void editStudent(User student) {
        Class selectedClass = classSelectionComboBox.getValue();
        if (selectedClass == null) {
            AlertHelper.showErrorAlert("Error", "Please select a class first.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/teacher/StudentForm.fxml"));
            Parent root = loader.load();
            
            StudentFormController controller = loader.getController();
            controller.setClass(selectedClass);
            controller.setStudent(student);
            controller.setOnSaveCallback(unused -> loadStudentsForClass(selectedClass));
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Student");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load student form", e);
            AlertHelper.showErrorAlert("Error", "Could not load student form: " + e.getMessage());
        }
    }
    
    private void removeStudent(User student) {
        Class selectedClass = classSelectionComboBox.getValue();
        if (selectedClass == null) {
            AlertHelper.showErrorAlert("Error", "Please select a class.");
            return;
        }
        
        boolean confirm = AlertHelper.showConfirmationAlert(
            "Confirm Remove", 
            "Are you sure you want to remove " + student.getFullName() + " from this class?"
        );
        
        if (confirm) {
            try {
                boolean success = DatabaseManager.getInstance().removeStudentFromClass(
                    student.getId(), selectedClass.getId());
                
                if (success) {
                    AlertHelper.showInformationAlert("Success", 
                        "Student removed from class successfully.");
                    loadStudentsForClass(selectedClass);
                } else {
                    AlertHelper.showErrorAlert("Error", 
                        "Failed to remove student from class.");
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to remove student", e);
                AlertHelper.showErrorAlert("Error", "Failed to remove student: " + e.getMessage());
            }
        }
    }
}
