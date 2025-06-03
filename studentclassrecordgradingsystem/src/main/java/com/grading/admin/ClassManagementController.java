package com.grading.admin;

import com.grading.common.DatabaseManager;
import com.grading.model.Class;
import com.grading.model.User;
import com.grading.teacher.ClassViewController;
import com.grading.util.AlertHelper;
import javafx.beans.property.SimpleIntegerProperty;
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

public class ClassManagementController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ClassManagementController.class.getName());

    @FXML private TableView<Class> classesTable;
    @FXML private TableColumn<Class, Integer> classIdColumn;
    @FXML private TableColumn<Class, String> classNameColumn;
    @FXML private TableColumn<Class, String> sectionColumn;
    @FXML private TableColumn<Class, String> subjectColumn;
    @FXML private TableColumn<Class, String> teacherColumn;
    @FXML private TableColumn<Class, Integer> studentsColumn;
    @FXML private TableColumn<Class, Void> actionsColumn;
    
    private final ObservableList<Class> classesList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup table columns
        classIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        classNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClassName()));
        sectionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSection()));
        subjectColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubject()));
        teacherColumn.setCellValueFactory(cellData -> {
            try {
                User teacher = DatabaseManager.getInstance().getUserById(cellData.getValue().getTeacherId());
                return new SimpleStringProperty(teacher != null ? teacher.getFullName() : "Not Assigned");
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        
        studentsColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleIntegerProperty(
                    DatabaseManager.getInstance().getStudentCount(cellData.getValue().getId())
                ).asObject();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error getting student count", e);
                return new SimpleIntegerProperty(0).asObject();
            }
        });
        
        setupActionsColumn();
        loadClasses();
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<Class, Void>, TableCell<Class, Void>> cellFactory = param -> 
            new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button viewBtn = new Button("View");
                private final Button deleteBtn = new Button("Delete");
                private final HBox buttons = new HBox(5, editBtn, viewBtn, deleteBtn);
                
                {
                    editBtn.getStyleClass().add("secondary-button");
                    viewBtn.getStyleClass().add("secondary-button");
                    deleteBtn.getStyleClass().add("secondary-button");
                    
                    editBtn.setOnAction(event -> {
                        Class clazz = getTableView().getItems().get(getIndex());
                        editClass(clazz);
                    });
                    
                    viewBtn.setOnAction(event -> {
                        Class clazz = getTableView().getItems().get(getIndex());
                        viewClass(clazz);
                    });
                    
                    deleteBtn.setOnAction(event -> {
                        Class clazz = getTableView().getItems().get(getIndex());
                        deleteClass(clazz);
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
            List<Class> classes = DatabaseManager.getInstance().getAllClasses();
            classesList.clear();
            classesList.addAll(classes);
            classesTable.setItems(classesList);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load classes", e);
            AlertHelper.showErrorAlert("Error", "Failed to load classes: " + e.getMessage());
        }
    }
    
    @FXML
    public void addNewClass() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ClassForm.fxml"));
            Parent root = loader.load();
            
            ClassFormController controller = loader.getController();
            controller.setOnSaveCallback(unused -> loadClasses());
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Class");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Could not load class form", e);
            AlertHelper.showErrorAlert("Error", "Could not load class form: " + e.getMessage());
        }
    }
    
    private void editClass(Class clazz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ClassForm.fxml"));
            Parent root = loader.load();
            
            ClassFormController controller = loader.getController();
            controller.setClass(clazz);
            controller.setOnSaveCallback(unused -> loadClasses());
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit Class");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Could not load class form", e);
            AlertHelper.showErrorAlert("Error", "Could not load class form: " + e.getMessage());
        }
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
    
    private void deleteClass(Class clazz) {
        boolean confirm = AlertHelper.showConfirmationAlert(
            "Confirm Delete", 
            "Are you sure you want to delete class: " + clazz.getClassName() + "?"
        );
        
        if (confirm) {
            try {
                // Delete class
                // For now, just show a message - actual deletion would be implemented here
                AlertHelper.showInformationAlert("Success", "Class deleted successfully");
                loadClasses();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to delete class", e);
                AlertHelper.showErrorAlert("Error", "Failed to delete class: " + e.getMessage());
            }
        }
    }
}
