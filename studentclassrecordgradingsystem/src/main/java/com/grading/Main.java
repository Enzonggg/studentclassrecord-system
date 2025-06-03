package com.grading;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.grading.common.DatabaseManager;
import com.grading.util.AlertHelper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize in-memory database
            if (!DatabaseManager.getInstance().initializeDatabase()) {
                AlertHelper.showErrorAlert("Initialization Error", 
                    "Failed to initialize the application data storage.\n" +
                    "The application will exit now.");
                System.exit(1);
            }
            
            // Load login screen
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/common/Login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            primaryStage.setTitle("Student Class Record Grading System");
            
            // Try to load the app icon, but continue if it's not found
            InputStream iconStream = getClass().getResourceAsStream("/images/app_icon.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            } else {
                LOGGER.log(Level.WARNING, "Application icon not found. Using default icon.");
            }
            
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start application", e);
            AlertHelper.showErrorAlert("Application Error", "Failed to start application: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Clean up resources
        DatabaseManager.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
