package com.grading.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Making class final prevents "Overridable method call in constructor" warnings
public final class Grade {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty studentId = new SimpleIntegerProperty(this, "studentId");
    private final IntegerProperty assignmentId = new SimpleIntegerProperty(this, "assignmentId");
    private final DoubleProperty score = new SimpleDoubleProperty(this, "score");
    private final StringProperty comments = new SimpleStringProperty(this, "comments");
    private final ObjectProperty<LocalDateTime> gradedAt = new SimpleObjectProperty<>(this, "gradedAt");
    
    // Additional properties for UI display
    private final StringProperty assignmentTitle = new SimpleStringProperty(this, "assignmentTitle");
    private final DoubleProperty maxScore = new SimpleDoubleProperty(this, "maxScore");
    private final IntegerProperty classId = new SimpleIntegerProperty(this, "classId");
    
    public Grade() {
    }
    
    public Grade(int id, int studentId, int assignmentId, double score, String comments, LocalDateTime gradedAt) {
        setId(id);
        setStudentId(studentId);
        setAssignmentId(assignmentId);
        setScore(score);
        setComments(comments);
        setGradedAt(gradedAt);
    }
    
    // Getters and setters
    public int getId() {
        return id.get();
    }
    
    public IntegerProperty idProperty() {
        return id;
    }
    
    public void setId(int id) {
        this.id.set(id);
    }
    
    public int getStudentId() {
        return studentId.get();
    }
    
    public IntegerProperty studentIdProperty() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId.set(studentId);
    }
    
    public int getAssignmentId() {
        return assignmentId.get();
    }
    
    public IntegerProperty assignmentIdProperty() {
        return assignmentId;
    }
    
    public void setAssignmentId(int assignmentId) {
        this.assignmentId.set(assignmentId);
    }
    
    public double getScore() {
        return score.get();
    }
    
    public DoubleProperty scoreProperty() {
        return score;
    }
    
    public void setScore(double score) {
        this.score.set(score);
    }
    
    public String getComments() {
        return comments.get();
    }
    
    public StringProperty commentsProperty() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments.set(comments);
    }
    
    public LocalDateTime getGradedAt() {
        return gradedAt.get();
    }
    
    public ObjectProperty<LocalDateTime> gradedAtProperty() {
        return gradedAt;
    }
    
    // This method is needed by StudentDashboardController for displaying dates in TableView
    public ObjectProperty<LocalDate> getGradedAtProperty() {
        ObjectProperty<LocalDate> dateProperty = new SimpleObjectProperty<>();
        if (gradedAt.get() != null) {
            dateProperty.set(gradedAt.get().toLocalDate());
        }
        return dateProperty;
    }
    
    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt.set(gradedAt);
    }
    
    public String getAssignmentTitle() {
        return assignmentTitle.get();
    }
    
    public StringProperty assignmentTitleProperty() {
        return assignmentTitle;
    }
    
    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle.set(assignmentTitle);
    }
    
    public double getMaxScore() {
        return maxScore.get();
    }
    
    public DoubleProperty maxScoreProperty() {
        return maxScore;
    }
    
    public void setMaxScore(double maxScore) {
        this.maxScore.set(maxScore);
    }
    
    public int getClassId() {
        return classId.get();
    }
    
    public IntegerProperty classIdProperty() {
        return classId;
    }
    
    public void setClassId(int classId) {
        this.classId.set(classId);
    }
}
