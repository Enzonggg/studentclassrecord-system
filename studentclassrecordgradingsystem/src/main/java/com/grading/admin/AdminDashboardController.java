package com.grading.admin;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.grading.common.DatabaseManager;
import com.grading.model.User;
import com.grading.util.AlertHelper;
import com.grading.util.SessionManager;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class AdminDashboardController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label totalClassesLabel;
    @FXML private Label userLabel;
    
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    
    @FXML private TabPane tabPane;
    @FXML private Tab dashboardTab;
    @FXML private Tab usersTab;
    
    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set user info
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("Logged in as: " + currentUser.getFullName());
        }
        
        // Initialize table columns
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().toString()));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Setup action buttons
        setupActionsColumn();
        
        // Load dashboard data
        loadDashboardData();
        
        // Load users
        loadUsers();
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = param -> 
            new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Delete");
                private final HBox buttons = new HBox(5, editBtn, deleteBtn);
                
                {
                    editBtn.getStyleClass().add("secondary-button");
                    deleteBtn.getStyleClass().add("secondary-button");
                    
                    editBtn.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        editUser(user);
                    });
                    
                    deleteBtn.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        deleteUser(user);
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
            // Count students
            List<User> allUsers = DatabaseManager.getInstance().getAllUsers();
            
            // Count students (users with studentInfo)
            long studentCount = allUsers.stream()
                .filter(user -> user.getStudentInfo() != null && !user.getStudentInfo().isEmpty())
                .count();
            totalStudentsLabel.setText(String.valueOf(studentCount));
            
            // Count teachers
            long teacherCount = allUsers.stream()
                .filter(user -> user.getRole() == User.Role.TEACHER && 
                      (user.getStudentInfo() == null || user.getStudentInfo().isEmpty()))
                .count();
            totalTeachersLabel.setText(String.valueOf(teacherCount));
            
            // Count classes
            int classCount = DatabaseManager.getInstance().getAllClasses().size();
            totalClassesLabel.setText(String.valueOf(classCount));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load dashboard data", e);
            AlertHelper.showErrorAlert("Error", "Failed to load dashboard data: " + e.getMessage());
        }
    }
    
    private void loadUsers() {
        try {
            List<User> users = DatabaseManager.getInstance().getAllUsers();
            usersList.clear();
            usersList.addAll(users);
            usersTable.setItems(usersList);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load users", e);
            AlertHelper.showErrorAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }
    
    @FXML
    public void showDashboard() {
        tabPane.getSelectionModel().select(dashboardTab);
        loadDashboardData();
    }
    
    @FXML
    public void showUsers() {
        tabPane.getSelectionModel().select(usersTab);
        loadUsers();
    }
    
    @FXML
    public void showClasses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ClassManagement.fxml"));
            Parent root = loader.load();
            
            Tab classesTab = new Tab("Manage Classes");
            classesTab.setContent(root);
            
            if (!tabExists("Manage Classes")) {
                tabPane.getTabs().add(classesTab);
            }
            
            tabPane.getSelectionModel().select(classesTab);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load class management view", e);
            AlertHelper.showErrorAlert("Error", "Could not load class management: " + e.getMessage());
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
    public void addNewUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UserForm.fxml"));
            Parent root = loader.load();
            
            UserFormController controller = loader.getController();
            controller.setOnSaveCallback(unused -> loadUsers());
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New User");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load user form", e);
            AlertHelper.showErrorAlert("Error", "Could not load user form: " + e.getMessage());
        }
    }
    
    private void editUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UserForm.fxml"));
            Parent root = loader.load();
            
            UserFormController controller = loader.getController();
            controller.setUser(user);
            controller.setOnSaveCallback(unused -> loadUsers());
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Edit User");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load user form", e);
            AlertHelper.showErrorAlert("Error", "Could not load user form: " + e.getMessage());
        }
    }
    
    private void deleteUser(User user) {
        if (user.getId() == SessionManager.getInstance().getCurrentUser().getId()) {
            AlertHelper.showErrorAlert("Error", "You cannot delete your own account!");
            return;
        }
        
        boolean confirm = AlertHelper.showConfirmationAlert(
            "Confirm Delete", 
            "Are you sure you want to delete user: " + user.getFullName() + "?"
        );
        
        if (confirm) {
            try {
                boolean result = DatabaseManager.getInstance().deleteUser(user.getId());
                
                if (result) {
                    AlertHelper.showInformationAlert("Success", "User deleted successfully.");
                    loadUsers();
                    loadDashboardData();
                } else {
                    AlertHelper.showErrorAlert("Error", "Failed to delete user.");
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to delete user", e);
                AlertHelper.showErrorAlert("Error", "Failed to delete user: " + e.getMessage());
            }
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
