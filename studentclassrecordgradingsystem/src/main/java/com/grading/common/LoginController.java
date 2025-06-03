package com.grading.common;

import com.grading.model.User;
import com.grading.util.AlertHelper;
import com.grading.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton; // Required by FXML binding, connected to Login.fxml

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // No initialization required
    }

    @FXML
    public void onLoginButtonClicked(ActionEvent event) {
        // Use loginButton to avoid "unused" warning
        loginButton.setDisable(true);
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            AlertHelper.showErrorAlert("Login Error", "Please enter both username and password");
            loginButton.setDisable(false);
            return;
        }

        try {
            User user = DatabaseManager.getInstance().authenticateUser(username, password);
            if (user != null) {
                // Store logged-in user in session
                SessionManager.getInstance().setCurrentUser(user);
                
                // Redirect to appropriate dashboard based on role
                String fxmlPath;
                switch (user.getRole()) {
                    case ADMIN:
                        fxmlPath = "/fxml/admin/AdminDashboard.fxml";
                        break;
                    case TEACHER:
                        fxmlPath = "/fxml/teacher/TeacherDashboard.fxml";
                        break;
                    default:
                        throw new IllegalStateException("Unknown role: " + user.getRole());
                }
                
                loadDashboard(event, fxmlPath);
            } else {
                AlertHelper.showErrorAlert("Login Error", "Invalid username or password");
            }
        } catch (IOException | IllegalStateException e) {
            // Using multicatch for both exception types
            LOGGER.log(Level.SEVERE, "Login error", e);
            AlertHelper.showErrorAlert("Login Error", "An error occurred: " + e.getMessage());
        } finally {
            // Re-enable button at the end
            loginButton.setDisable(false);
        }
    }

    private void loadDashboard(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
