package com.grading.common;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.grading.model.Assignment;
import com.grading.model.Attendance;
import com.grading.model.Class;
import com.grading.model.Grade;
import com.grading.model.User;

public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    
    private static DatabaseManager instance;
    
    // In-memory storage
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Class> classes = new HashMap<>();
    private final Map<Integer, List<Integer>> studentClasses = new HashMap<>(); // student_id -> list of class_ids
    private final Map<Integer, Assignment> assignments = new HashMap<>();
    private final Map<Integer, Grade> grades = new HashMap<>();
    private final Map<Integer, Attendance> attendances = new HashMap<>();
    
    // ID generators
    private final AtomicInteger userIdGenerator = new AtomicInteger(1);
    private final AtomicInteger classIdGenerator = new AtomicInteger(1);
    private final AtomicInteger assignmentIdGenerator = new AtomicInteger(1);
    private final AtomicInteger gradeIdGenerator = new AtomicInteger(1);
    private final AtomicInteger attendanceIdGenerator = new AtomicInteger(1);
    
    private DatabaseManager() {
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public boolean initializeDatabase() {
        try {
            // Create default admin user if not exists
            if (users.isEmpty()) {
                createDefaultData();
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize in-memory database", e);
            return false;
        }
    }
    
    private void createDefaultData() {
        // Create admin user
        User admin = new User(
            userIdGenerator.getAndIncrement(),
            "admin",
            "admin123",
            User.Role.ADMIN,
            "System Administrator",
            "admin@school.edu",
            LocalDateTime.now(),
            null  // studentId is null for admin
        );
        users.put(admin.getId(), admin);
        
        // Create test teacher
        User teacher = new User(
            userIdGenerator.getAndIncrement(),
            "teacher",
            "teacher123",
            User.Role.TEACHER,
            "John Smith",
            "jsmith@school.edu",
            LocalDateTime.now(),
            null  // studentId is null for teacher
        );
        users.put(teacher.getId(), teacher);
        
        // Create test students (now managed by teachers/admin)
        User student1 = new User(
            userIdGenerator.getAndIncrement(),
            "student1",
            "student123",
            User.Role.TEACHER, // Changed from STUDENT to TEACHER for testing
            "Jane Doe",
            "jdoe@school.edu",
            LocalDateTime.now(),
            "Student ID: S1001, Grade: 10"
        );
        users.put(student1.getId(), student1);
        
        // Create multiple test classes for the teacher
        createTestClass(teacher.getId(), "Mathematics 101", "Section A", "Math", "2024-2025");
        createTestClass(teacher.getId(), "Science 101", "Section B", "Science", "2024-2025");
        createTestClass(teacher.getId(), "English 101", "Section C", "English", "2024-2025");
        
        // Get the first class for enrolling the student
        Class mathClass = classes.values().stream()
            .filter(c -> c.getClassName().equals("Mathematics 101"))
            .findFirst()
            .orElse(null);
        
        if (mathClass != null) {
            // Enroll student in class
            enrollStudent(student1.getId(), mathClass.getId());
            
            // Create test assignment
            Assignment assignment = new Assignment(
                assignmentIdGenerator.getAndIncrement(),
                mathClass.getId(),
                "Midterm Exam",
                "Covers chapters 1-5",
                Assignment.Type.EXAM,
                100.0,
                30.0,
                LocalDate.now().plusDays(7),
                LocalDateTime.now()
            );
            assignments.put(assignment.getId(), assignment);
        }
    }

    private void createTestClass(int teacherId, String className, String section, String subject, String schoolYear) {
        Class newClass = new Class(
            classIdGenerator.getAndIncrement(),
            className,
            section,
            subject,
            teacherId,
            schoolYear
        );
        classes.put(newClass.getId(), newClass);
        LOGGER.log(Level.INFO, "Created test class: {0} for teacher ID: {1}", new Object[]{className, teacherId});
    }
    
    // User methods
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    public User getUserById(int id) {
        return users.get(id);
    }
    
    public User getUserByUsername(String username) {
        return users.values().stream()
            .filter(user -> user.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }
    
    public User authenticateUser(String username, String password) {
        return users.values().stream()
            .filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password))
            .findFirst()
            .orElse(null);
    }
    
    public boolean createUser(User user) {
        try {
            if (user.getId() == 0) {
                user.setId(userIdGenerator.getAndIncrement());
            }
            users.put(user.getId(), user);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating user", e);
            return false;
        }
    }
    
    public boolean updateUser(User user) {
        try {
            users.put(user.getId(), user);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating user", e);
            return false;
        }
    }
    
    public boolean deleteUser(int userId) {
        try {
            users.remove(userId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting user", e);
            return false;
        }
    }
    
    // Class methods
    public List<Class> getAllClasses() {
        return new ArrayList<>(classes.values());
    }
    
    public List<Class> getClassesByTeacher(int teacherId) {
        List<Class> teacherClasses = classes.values().stream()
            .filter(clazz -> clazz.getTeacherId() == teacherId)
            .collect(Collectors.toList());
        
        // Debug log to trace the issue
        LOGGER.log(Level.INFO, "Found {0} classes for teacher ID {1}", new Object[]{teacherClasses.size(), teacherId});
        
        return teacherClasses;
    }
    
    public List<Class> getClassesByStudent(int studentId) {
        List<Integer> classIds = studentClasses.getOrDefault(studentId, new ArrayList<>());
        return classIds.stream()
            .map(classes::get)
            .collect(Collectors.toList());
    }
    
    public int getStudentCount(int classId) {
        return (int) studentClasses.entrySet().stream()
            .filter(entry -> entry.getValue().contains(classId))
            .count();
    }
    
    public boolean createClass(Class clazz) {
        try {
            if (clazz.getId() == 0) {
                clazz.setId(classIdGenerator.getAndIncrement());
            }
            classes.put(clazz.getId(), clazz);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating class", e);
            return false;
        }
    }
    
    public boolean enrollStudent(int studentId, int classId) {
        try {
            List<Integer> studentClassIds = studentClasses.getOrDefault(studentId, new ArrayList<>());
            if (!studentClassIds.contains(classId)) {
                studentClassIds.add(classId);
                studentClasses.put(studentId, studentClassIds);
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error enrolling student", e);
            return false;
        }
    }
    
    public boolean removeStudentFromClass(int studentId, int classId) {
        try {
            List<Integer> studentClassIds = studentClasses.getOrDefault(studentId, new ArrayList<>());
            
            // Log for debugging
            LOGGER.log(Level.INFO, "Removing student ID {0} from class ID {1}", new Object[]{studentId, classId});
            LOGGER.log(Level.INFO, "Current classes for student: {0}", new Object[]{studentClassIds});
            
            if (studentClassIds.contains(classId)) {
                studentClassIds.remove(Integer.valueOf(classId)); // Use valueOf to remove object, not index
                studentClasses.put(studentId, studentClassIds);
                LOGGER.log(Level.INFO, "Removed student ID {0} from class ID {1}", new Object[]{studentId, classId});
                return true;
            } else {
                LOGGER.info("Student is not enrolled in this class");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing student from class", e);
            return false;
        }
    }
    
    // Assignment methods
    public List<Assignment> getAssignmentsByClass(int classId) {
        return assignments.values().stream()
            .filter(assignment -> assignment.getClassId() == classId)
            .collect(Collectors.toList());
    }
    
    public boolean createAssignment(Assignment assignment) {
        try {
            if (assignment.getId() == 0) {
                assignment.setId(assignmentIdGenerator.getAndIncrement());
            }
            assignments.put(assignment.getId(), assignment);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating assignment", e);
            return false;
        }
    }
    
    // Grade methods
    public List<Grade> getGradesByStudent(int studentId) {
        return grades.values().stream()
            .filter(grade -> grade.getStudentId() == studentId)
            .collect(Collectors.toList());
    }
    
    public List<Grade> getGradesByAssignment(int assignmentId) {
        return grades.values().stream()
            .filter(grade -> grade.getAssignmentId() == assignmentId)
            .collect(Collectors.toList());
    }
    
    public boolean createGrade(Grade grade) {
        try {
            if (grade.getId() == 0) {
                grade.setId(gradeIdGenerator.getAndIncrement());
            }
            grades.put(grade.getId(), grade);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating grade", e);
            return false;
        }
    }
    
    // Attendance methods
    public List<Attendance> getAttendanceByClass(int classId, LocalDate date) {
        return attendances.values().stream()
            .filter(attendance -> attendance.getClassId() == classId && 
                    (date == null || attendance.getDate().equals(date)))
            .collect(Collectors.toList());
    }
    
    public List<Attendance> getAttendanceByStudent(int studentId) {
        return attendances.values().stream()
            .filter(attendance -> attendance.getStudentId() == studentId)
            .collect(Collectors.toList());
    }
    
    public boolean createAttendance(Attendance attendance) {
        try {
            if (attendance.getId() == 0) {
                attendance.setId(attendanceIdGenerator.getAndIncrement());
            }
            attendances.put(attendance.getId(), attendance);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating attendance record", e);
            return false;
        }
    }
    
    public boolean updateAttendance(Attendance attendance) {
        try {
            attendances.put(attendance.getId(), attendance);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating attendance record", e);
            return false;
        }
    }
    
    public boolean deleteAttendance(int attendanceId) {
        try {
            attendances.remove(attendanceId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting attendance record", e);
            return false;
        }
    }
    
    // Get student IDs enrolled in a class
    public List<Integer> getStudentIdsForClass(int classId) {
        List<Integer> studentIds = new ArrayList<>();
        
        // Loop through studentClasses and find students enrolled in this class
        for (Map.Entry<Integer, List<Integer>> entry : studentClasses.entrySet()) {
            if (entry.getValue().contains(classId)) {
                studentIds.add(entry.getKey());
            }
        }
        
        return studentIds;
    }
    
    // Get grade by student and assignment
    public Grade getGradeByStudentAndAssignment(int studentId, int assignmentId) {
        return grades.values().stream()
            .filter(grade -> grade.getStudentId() == studentId && grade.getAssignmentId() == assignmentId)
            .findFirst()
            .orElse(null);
    }
    
    // Update grade - creates a new one if not exists, updates if exists
    public boolean updateGrade(Grade grade) {
        try {
            // Check if grade already exists
            Grade existingGrade = getGradeByStudentAndAssignment(grade.getStudentId(), grade.getAssignmentId());
            
            if (existingGrade != null) {
                // Update existing grade
                existingGrade.setScore(grade.getScore());
                existingGrade.setComments(grade.getComments());
                existingGrade.setGradedAt(LocalDateTime.now());
                grades.put(existingGrade.getId(), existingGrade);
            } else {
                // Create new grade
                if (grade.getId() == 0) {
                    grade.setId(gradeIdGenerator.getAndIncrement());
                }
                grades.put(grade.getId(), grade);
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating grade", e);
            return false;
        }
    }
    
    public Connection getConnection() {
        // This is a temporary solution to make the transition to in-memory storage easier
        // Eventually, all controller code should be updated to use the direct methods above
        throw new UnsupportedOperationException(
            "Direct SQL connections are no longer supported. " + 
            "Please use the provided methods for data access instead.");
    }
    
    public void closeConnection() {
        // Nothing to do for in-memory database
    }

    // Add method to get students info list
    public List<User> getStudentInfoList() {
        return users.values().stream()
            .filter(user -> user.getStudentInfo() != null && !user.getStudentInfo().isEmpty())
            .collect(Collectors.toList());
    }
}
