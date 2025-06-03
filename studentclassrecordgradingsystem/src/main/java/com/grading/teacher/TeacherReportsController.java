package com.grading.teacher;

import com.grading.common.DatabaseManager;
import com.grading.model.Assignment;
import com.grading.model.Attendance;
import com.grading.model.Class;
import com.grading.model.Grade;
import com.grading.model.User;
import com.grading.util.AlertHelper;
import com.grading.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TeacherReportsController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(TeacherReportsController.class.getName());

    @FXML private ComboBox<Class> classSelectionComboBox;
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private Button generateReportBtn;
    @FXML private Button exportReportBtn;
    
    // Performance report table
    @FXML private TableView<StudentPerformance> performanceReportTable;
    @FXML private TableColumn<StudentPerformance, String> studentIdColumn;
    @FXML private TableColumn<StudentPerformance, String> studentNameColumn;
    @FXML private TableColumn<StudentPerformance, String> overallGradeColumn;
    @FXML private TableColumn<StudentPerformance, String> quizzesGradeColumn;
    @FXML private TableColumn<StudentPerformance, String> examsGradeColumn;
    @FXML private TableColumn<StudentPerformance, String> assignmentsGradeColumn;
    
    // Attendance report table
    @FXML private TableView<StudentAttendance> attendanceReportTable;
    @FXML private TableColumn<StudentAttendance, String> attStudentIdColumn;
    @FXML private TableColumn<StudentAttendance, String> attStudentNameColumn;
    @FXML private TableColumn<StudentAttendance, String> presentDaysColumn;
    @FXML private TableColumn<StudentAttendance, String> absentDaysColumn;
    @FXML private TableColumn<StudentAttendance, String> lateDaysColumn;
    @FXML private TableColumn<StudentAttendance, String> attendanceRateColumn;
    
    private final ObservableList<Class> classesList = FXCollections.observableArrayList();
    private final ObservableList<StudentPerformance> performanceList = FXCollections.observableArrayList();
    private final ObservableList<StudentAttendance> attendanceList = FXCollections.observableArrayList();
    
    // Inner classes for report data
    public static class StudentPerformance {
        private final String studentId;
        private final String studentName;
        private final String overallGrade;
        private final String quizzesGrade;
        private final String examsGrade;
        private final String assignmentsGrade;
        
        public StudentPerformance(String studentId, String studentName, String overallGrade, 
                                 String quizzesGrade, String examsGrade, String assignmentsGrade) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.overallGrade = overallGrade;
            this.quizzesGrade = quizzesGrade;
            this.examsGrade = examsGrade;
            this.assignmentsGrade = assignmentsGrade;
        }
        
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getOverallGrade() { return overallGrade; }
        public String getQuizzesGrade() { return quizzesGrade; }
        public String getExamsGrade() { return examsGrade; }
        public String getAssignmentsGrade() { return assignmentsGrade; }
    }
    
    public static class StudentAttendance {
        private final String studentId;
        private final String studentName;
        private final int presentDays;
        private final int absentDays;
        private final int lateDays;
        private final double attendanceRate;
        
        public StudentAttendance(String studentId, String studentName, int presentDays, 
                               int absentDays, int lateDays, double attendanceRate) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.presentDays = presentDays;
            this.absentDays = absentDays;
            this.lateDays = lateDays;
            this.attendanceRate = attendanceRate;
        }
        
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public int getPresentDays() { return presentDays; }
        public int getAbsentDays() { return absentDays; }
        public int getLateDays() { return lateDays; }
        public double getAttendanceRate() { return attendanceRate; }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize report types dropdown
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
            "Performance Report",
            "Attendance Report",
            "Assignment Completion Report"
        ));
        reportTypeComboBox.getSelectionModel().selectFirst();
        
        // Initialize performance report table columns
        studentIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStudentId()));
        studentNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStudentName()));
        overallGradeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOverallGrade()));
        quizzesGradeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getQuizzesGrade()));
        examsGradeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getExamsGrade()));
        assignmentsGradeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAssignmentsGrade()));
        
        // Initialize attendance report table columns
        attStudentIdColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStudentId()));
        attStudentNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStudentName()));
        presentDaysColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getPresentDays())));
        absentDaysColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getAbsentDays())));
        lateDaysColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getLateDays())));
        attendanceRateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.1f%%", cellData.getValue().getAttendanceRate())));
        
        // Initialize UI components to address "unused variable" warnings
        generateReportBtn.setDefaultButton(true);
        exportReportBtn.setCancelButton(false);
        
        // Load classes for this teacher
        loadClasses();
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
    
    @FXML
    public void generateReport() {
        Class selectedClass = classSelectionComboBox.getValue();
        String reportType = reportTypeComboBox.getValue();
        
        if (selectedClass == null) {
            AlertHelper.showErrorAlert("Error", "Please select a class.");
            return;
        }
        
        if (reportType == null) {
            AlertHelper.showErrorAlert("Error", "Please select a report type.");
            return;
        }
        
        // Generate real data instead of sample data
        generateReportData(selectedClass, reportType);
        
        LOGGER.info(() -> "Report generated for " + selectedClass.getClassName() + ": " + reportType);
    }
    
    private void generateReportData(Class selectedClass, String reportType) {
        // Clear existing data
        performanceList.clear();
        attendanceList.clear();
        
        try {
            // Get students enrolled in this class
            List<Integer> studentIds = DatabaseManager.getInstance().getStudentIdsForClass(selectedClass.getId());
            
            // Get assignments for this class
            List<Assignment> assignments = DatabaseManager.getInstance().getAssignmentsByClass(selectedClass.getId());
            
            if (studentIds.isEmpty()) {
                AlertHelper.showInformationAlert("No Students", 
                    "There are no students enrolled in this class.");
                return;
            }
            
            if (assignments.isEmpty() && reportType.contains("Performance")) {
                AlertHelper.showInformationAlert("No Assignments", 
                    "There are no assignments in this class.");
                return;
            }
            
            // Define passing threshold
            final double PASSING_THRESHOLD = 60.0; // 60% is passing
            
            // Process each student
            for (Integer studentId : studentIds) {
                User student = DatabaseManager.getInstance().getUserById(studentId);
                if (student == null) continue;
                
                // Extract student ID from studentInfo
                String studentIdStr = "N/A";
                if (student.getStudentInfo() != null && student.getStudentInfo().contains("Student ID:")) {
                    studentIdStr = student.getStudentInfo().substring(
                        student.getStudentInfo().indexOf("Student ID:") + 11,
                        student.getStudentInfo().contains(",") ? 
                        student.getStudentInfo().indexOf(",") : student.getStudentInfo().length()
                    ).trim();
                }
                
                if (reportType.contains("Performance")) {
                    // Calculate performance metrics
                    double totalScore = 0;
                    double totalMaxScore = 0;
                    double quizScore = 0, quizMaxScore = 0;
                    double examScore = 0, examMaxScore = 0;
                    double assignmentScore = 0, assignmentMaxScore = 0;
                    int passedItems = 0;
                    int totalItems = 0;
                    
                    for (Assignment assignment : assignments) {
                        Grade grade = DatabaseManager.getInstance()
                            .getGradeByStudentAndAssignment(studentId, assignment.getId());
                        
                        if (grade != null) {
                            double score = grade.getScore();
                            double maxScore = assignment.getMaxScore();
                            double percentage = (score / maxScore) * 100;
                            
                            // Count passed items
                            if (percentage >= PASSING_THRESHOLD) {
                                passedItems++;
                            }
                            totalItems++;
                            
                            totalScore += score;
                            totalMaxScore += maxScore;
                            
                            switch (assignment.getType()) {
                                case QUIZ:
                                    quizScore += score;
                                    quizMaxScore += maxScore;
                                    break;
                                case EXAM:
                                    examScore += score;
                                    examMaxScore += maxScore;
                                    break;
                                case ASSIGNMENT:
                                case PROJECT:
                                    assignmentScore += score;
                                    assignmentMaxScore += maxScore;
                                    break;
                            }
                        }
                    }
                    
                    // Calculate percentages
                    double overallGrade = totalMaxScore > 0 ? (totalScore / totalMaxScore) * 100 : 0;
                    double quizGrade = quizMaxScore > 0 ? (quizScore / quizMaxScore) * 100 : 0;
                    double examGrade = examMaxScore > 0 ? (examScore / examMaxScore) * 100 : 0;
                    double assignmentGrade = assignmentMaxScore > 0 ? 
                        (assignmentScore / assignmentMaxScore) * 100 : 0;
                    
                    // Calculate passing rate (percentage of passed items)
                    double passingRate = totalItems > 0 ? (passedItems / (double)totalItems) * 100 : 0;
                    
                    // Include total score in student name for clarity in reports
                    String displayName = student.getFullName();
                    if (totalMaxScore > 0) {
                        displayName += String.format(" (%.1f/%.1f - %.1f%%)", 
                            totalScore, totalMaxScore, overallGrade);
                    }
                    
                    // Add passingRate to overall grade string
                    String displayOverallGrade = String.format("%.1f%% (Pass Rate: %.1f%%)", 
                        overallGrade, passingRate);
                    
                    // Add formatted grade values to performance list
                    performanceList.add(new StudentPerformance(
                        studentIdStr,
                        displayName,
                        displayOverallGrade,
                        String.format("%.1f%%", quizGrade),
                        String.format("%.1f%%", examGrade),
                        String.format("%.1f%%", assignmentGrade)
                    ));
                }
                
                if (reportType.contains("Attendance")) {
                    // Get attendance records for this student
                    List<Attendance> attendanceRecords = DatabaseManager.getInstance()
                        .getAttendanceByStudent(studentId);
                    
                    // Filter to only include records for this class
                    attendanceRecords = attendanceRecords.stream()
                        .filter(a -> a.getClassId() == selectedClass.getId())
                        .collect(Collectors.toList());
                    
                    int presentDays = 0, absentDays = 0, lateDays = 0;
                    
                    for (Attendance record : attendanceRecords) {
                        switch (record.getStatus()) {
                            case "Present":
                                presentDays++;
                                break;
                            case "Absent":
                                absentDays++;
                                break;
                            case "Late":
                                lateDays++;
                                break;
                        }
                    }
                    
                    int totalDays = presentDays + absentDays + lateDays;
                    double attendanceRate = totalDays > 0 ? 
                        ((double) presentDays / totalDays) * 100 : 0;
                    
                    attendanceList.add(new StudentAttendance(
                        studentIdStr,
                        student.getFullName(),
                        presentDays,
                        absentDays,
                        lateDays,
                        attendanceRate
                    ));
                }
            }
            
            // Sort performance list by overall grade (highest first)
            if (!performanceList.isEmpty()) {
                // Sort by actual numeric value, not display string
                performanceList.sort((a, b) -> {
                    // Extract numeric percentage from display string
                    double aGrade = extractPercentage(a.getOverallGrade());
                    double bGrade = extractPercentage(b.getOverallGrade());
                    return Double.compare(bGrade, aGrade);
                });
                
                // Find highest score student and add indicator
                if (!performanceList.isEmpty()) {
                    StudentPerformance topStudent = performanceList.get(0);
                    performanceList.set(0, new StudentPerformance(
                        topStudent.getStudentId() + " ⭐",
                        topStudent.getStudentName() + " (Highest)",
                        topStudent.getOverallGrade(),
                        topStudent.getQuizzesGrade(),
                        topStudent.getExamsGrade(),
                        topStudent.getAssignmentsGrade()
                    ));
                }
            }
            
            // Sort attendance list by attendance rate (highest first)
            if (!attendanceList.isEmpty()) {
                attendanceList.sort((a, b) -> Double.compare(b.getAttendanceRate(), a.getAttendanceRate()));
            }
            
            // Set data to tables
            performanceReportTable.setItems(performanceList);
            attendanceReportTable.setItems(attendanceList);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating report", e);
            AlertHelper.showErrorAlert("Error", "Failed to generate report: " + e.getMessage());
        }
    }
    
    // Helper method to extract percentage from formatted string
    private double extractPercentage(String formattedGrade) {
        try {
            // Extract the first percentage value from a string like "85.5% (Pass Rate: 90.0%)"
            int endIndex = formattedGrade.indexOf('%');
            if (endIndex > 0) {
                String percentStr = formattedGrade.substring(0, endIndex).trim();
                return Double.parseDouble(percentStr);
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            LOGGER.log(Level.WARNING, "Error parsing percentage from: " + formattedGrade, e);
        }
        return 0.0;
    }
    
    @FXML
    public void exportReport() {
        // Reference selectedClass to avoid unused variable warning
        Class selectedClass = classSelectionComboBox.getValue();
        if (selectedClass != null) {
            LOGGER.info(() -> "Exporting report for class: " + selectedClass.getClassName());
        }
        
        LOGGER.info("Report export requested");
        AlertHelper.showInformationAlert("Coming Soon", 
            "Export functionality will be available in the next update.");
    }
}
