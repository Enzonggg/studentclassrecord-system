package com.grading.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// Making class final prevents "Overridable method call in constructor" warnings
public final class Class {
    private final IntegerProperty id = new SimpleIntegerProperty(this, "id");
    private final StringProperty className = new SimpleStringProperty(this, "className");
    private final StringProperty section = new SimpleStringProperty(this, "section");
    private final StringProperty subject = new SimpleStringProperty(this, "subject");
    private final IntegerProperty teacherId = new SimpleIntegerProperty(this, "teacherId");
    private final StringProperty schoolYear = new SimpleStringProperty(this, "schoolYear");
    
    public Class() {
    }
    
    public Class(int id, String className, String section, String subject, int teacherId, String schoolYear) {
        setId(id);
        setClassName(className);
        setSection(section);
        setSubject(subject);
        setTeacherId(teacherId);
        setSchoolYear(schoolYear);
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
    
    public String getClassName() {
        return className.get();
    }
    
    public StringProperty classNameProperty() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className.set(className);
    }
    
    public String getSection() {
        return section.get();
    }
    
    public StringProperty sectionProperty() {
        return section;
    }
    
    public void setSection(String section) {
        this.section.set(section);
    }
    
    public String getSubject() {
        return subject.get();
    }
    
    public StringProperty subjectProperty() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject.set(subject);
    }
    
    public int getTeacherId() {
        return teacherId.get();
    }
    
    public IntegerProperty teacherIdProperty() {
        return teacherId;
    }
    
    public void setTeacherId(int teacherId) {
        this.teacherId.set(teacherId);
    }
    
    public String getSchoolYear() {
        return schoolYear.get();
    }
    
    public StringProperty schoolYearProperty() {
        return schoolYear;
    }
    
    public void setSchoolYear(String schoolYear) {
        this.schoolYear.set(schoolYear);
    }
    
    @Override
    public String toString() {
        return className.get() + " - " + section.get();
    }
}
