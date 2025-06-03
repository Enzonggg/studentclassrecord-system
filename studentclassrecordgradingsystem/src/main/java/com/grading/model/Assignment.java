package com.grading.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Making class final prevents "Overridable method call in constructor" warnings
public final class Assignment {
    
    public enum Type {
        QUIZ, EXAM, ASSIGNMENT, PROJECT
    }
    
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final IntegerProperty classId = new SimpleIntegerProperty(this, "classId");
    private final StringProperty title = new SimpleStringProperty(this, "title");
    private final StringProperty description = new SimpleStringProperty(this, "description");
    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(this, "type");
    private final DoubleProperty maxScore = new SimpleDoubleProperty(this, "maxScore");
    private final DoubleProperty weight = new SimpleDoubleProperty(this, "weight");
    private final ObjectProperty<LocalDate> dueDate = new SimpleObjectProperty<>(this, "dueDate");
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>(this, "createdAt");
    
    public Assignment() {
    }
    
    public Assignment(int id, int classId, String title, String description, Type type, 
                     double maxScore, double weight, LocalDate dueDate, LocalDateTime createdAt) {
        setId(id);
        setClassId(classId);
        setTitle(title);
        setDescription(description);
        setType(type);
        setMaxScore(maxScore);
        setWeight(weight);
        setDueDate(dueDate);
        setCreatedAt(createdAt);
    }
    
    public int getId() {
        return id.get();
    }
    
    public IntegerProperty idProperty() {
        return id;
    }
    
    public void setId(int id) {
        this.id.set(id);
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
    
    public String getTitle() {
        return title.get();
    }
    
    public StringProperty titleProperty() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title.set(title);
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public Type getType() {
        return type.get();
    }
    
    public ObjectProperty<Type> typeProperty() {
        return type;
    }
    
    public void setType(Type type) {
        this.type.set(type);
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
    
    public double getWeight() {
        return weight.get();
    }
    
    public DoubleProperty weightProperty() {
        return weight;
    }
    
    public void setWeight(double weight) {
        this.weight.set(weight);
    }
    
    public LocalDate getDueDate() {
        return dueDate.get();
    }
    
    public ObjectProperty<LocalDate> dueDateProperty() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate.set(dueDate);
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }
    
    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }
    
    @Override
    public String toString() {
        return title.get();
    }
}
