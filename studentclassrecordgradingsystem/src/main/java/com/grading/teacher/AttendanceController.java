package com.grading.teacher;

import com.grading.common.DatabaseManager;
import com.grading.model.Attendance;
import com.grading.model.Class;
import com.grading.model.User;
import com.grading.util.AlertHelper;
import com.grading.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttendanceController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(AttendanceController.class.getName());

    @FXML private ComboBox<Class> classSelectionComboBox;
    @FXML private DatePicker attendanceDatePicker;
    
    @FXML private TableView<StudentAttendance> attendanceTable;
    @FXML private TableColumn<StudentAttendance, String> studentIdColumn;
    @FXML private TableColumn<StudentAttendance, String> studentNameColumn;
    @FXML private TableColumn<StudentAttendance, String> statusColumn;
    @FXML private TableColumn<StudentAttendance, String> remarksColumn;
    
    private final ObservableList<Class> classesList = FXCollections.observableArrayList();
    private final ObservableList<StudentAttendance> attendanceList = FXCollections.observableArrayList();
    
    // Inner class for student attendance records
    public static class StudentAttendance {
        private final int studentId;
        private final String studentName;
        private String status;
        private String remarks;
        
        public StudentAttendance(int studentId, String studentName, String status, String remarks) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.status = status;
            this.remarks = remarks;
        }
        
        public int getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        studentIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getStudentId())));
            
        studentNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStudentName()));
        
        // Create a combo box for status selection
        statusColumn.setCellFactory(column -> {
            return new TableCell<StudentAttendance, String>() {
                private final ComboBox<String> comboBox = new ComboBox<>();
                
                {
                    comboBox.getItems().addAll("Present", "Absent", "Late", "Excused");
                    comboBox.setOnAction(event -> {
                        StudentAttendance attendance = getTableRow().getItem();
                        if (attendance != null) {
                            attendance.setStatus(comboBox.getValue());
                        }
                    });
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        comboBox.setValue(item);
                        setGraphic(comboBox);
                    }
                }
            };
        });
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Create a text field for remarks
        remarksColumn.setCellFactory(column -> {
            return new TableCell<StudentAttendance, String>() {
                private final TextField textField = new TextField();
                
                {
                    textField.setOnAction(event -> {
                        StudentAttendance attendance = getTableRow().getItem();
                        if (attendance != null) {
                            attendance.setRemarks(textField.getText());
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            StudentAttendance attendance = getTableRow().getItem();
                            if (attendance != null) {
                                attendance.setRemarks(textField.getText());
                            }
                        }
                    });
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        textField.setText(item);
                        setGraphic(textField);
                    }
                }
            };
        });
        
        remarksColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRemarks()));
        
        // Set today's date and disable future dates
        attendanceDatePicker.setValue(LocalDate.now());
        attendanceDatePicker.setDayCellFactory(getDatePickerDayCellFactory());
        
        // Load classes for this teacher
        loadClasses();
        
        // Set up class selection listener
        classSelectionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAttendanceForClass(newVal);
            }
        });
    }
    
    private Callback<DatePicker, DateCell> getDatePickerDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                
                // Disable future dates
                if (item.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f0f0f0;");
                }
            }
        };
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
    
    private void loadAttendanceForClass(Class selectedClass) {
        try {
            LocalDate selectedDate = attendanceDatePicker.getValue();
            if (selectedDate == null) {
                AlertHelper.showErrorAlert("Error", "Please select a date for attendance.");
                return;
            }
            
            // Check if date is in the future
            if (selectedDate.isAfter(LocalDate.now())) {
                AlertHelper.showErrorAlert("Error", "Cannot record attendance for future dates.");
                return;
            }
            
            // Get existing attendance records for this class and date
            List<Attendance> existingRecords = DatabaseManager.getInstance()
                .getAttendanceByClass(selectedClass.getId(), selectedDate);
            
            // Get students enrolled in this class
            List<Integer> studentIds = DatabaseManager.getInstance().getStudentIdsForClass(selectedClass.getId());
            
            attendanceList.clear();
            
            // Log for debugging purposes
            LOGGER.log(Level.INFO, "Loading attendance for class ID: {0}, Found {1} enrolled students", 
                     new Object[]{selectedClass.getId(), studentIds.size()});
            
            // For each student, create an attendance record
            for (Integer studentId : studentIds) {
                User student = DatabaseManager.getInstance().getUserById(studentId);
                if (student != null) {
                    LOGGER.log(Level.INFO, "Processing student: {0} (ID: {1})", 
                             new Object[]{student.getFullName(), studentId});
                    
                    // Find existing record for this student on this date
                    Attendance existingRecord = existingRecords.stream()
                        .filter(a -> a.getStudentId() == studentId)
                        .findFirst()
                        .orElse(null);
                    
                    String status = existingRecord != null ? existingRecord.getStatus() : "Present";
                    String remarks = existingRecord != null ? existingRecord.getRemarks() : "";
                    
                    attendanceList.add(new StudentAttendance(
                        studentId, 
                        student.getFullName(), 
                        status, 
                        remarks
                    ));
                }
            }
            
            attendanceTable.setItems(attendanceList);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load attendance", e);
            AlertHelper.showErrorAlert("Error", "Failed to load attendance: " + e.getMessage());
        }
    }
    
    @FXML
    public void loadAttendance() {
        Class selectedClass = classSelectionComboBox.getValue();
        
        if (selectedClass == null) {
            AlertHelper.showErrorAlert("Error", "Please select a class.");
            return;
        }
        
        loadAttendanceForClass(selectedClass);
    }
    
    @FXML
    public void saveAttendance() {
        try {
            Class selectedClass = classSelectionComboBox.getValue();
            LocalDate selectedDate = attendanceDatePicker.getValue();
            
            if (selectedClass == null) {
                AlertHelper.showErrorAlert("Error", "Please select a class.");
                return;
            }
            
            if (selectedDate == null) {
                AlertHelper.showErrorAlert("Error", "Please select a date.");
                return;
            }
            
            // Check if date is in the future
            if (selectedDate.isAfter(LocalDate.now())) {
                AlertHelper.showErrorAlert("Error", "Cannot record attendance for future dates.");
                return;
            }
            
            // Get existing attendance records for this class and date to avoid duplicates
            List<Attendance> existingRecords = DatabaseManager.getInstance()
                .getAttendanceByClass(selectedClass.getId(), selectedDate);
            
            // Process each attendance record
            for (StudentAttendance studentAttendance : attendanceList) {
                // Check if record already exists for this student
                Attendance existingRecord = existingRecords.stream()
                    .filter(a -> a.getStudentId() == studentAttendance.getStudentId())
                    .findFirst()
                    .orElse(null);
                
                if (existingRecord != null) {
                    // Update existing record
                    existingRecord.setStatus(studentAttendance.getStatus());
                    existingRecord.setRemarks(studentAttendance.getRemarks());
                    existingRecord.setRecordedAt(LocalDateTime.now());
                    DatabaseManager.getInstance().updateAttendance(existingRecord);
                } else {
                    // Create new record
                    Attendance newRecord = new Attendance(
                        0, // ID will be assigned by DatabaseManager
                        studentAttendance.getStudentId(),
                        selectedClass.getId(),
                        selectedDate,
                        studentAttendance.getStatus(),
                        studentAttendance.getRemarks(),
                        LocalDateTime.now()
                    );
                    DatabaseManager.getInstance().createAttendance(newRecord);
                }
            }
            
            AlertHelper.showInformationAlert("Success", "Attendance records saved successfully.");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save attendance", e);
            AlertHelper.showErrorAlert("Error", "Failed to save attendance: " + e.getMessage());
        }
    }
}
