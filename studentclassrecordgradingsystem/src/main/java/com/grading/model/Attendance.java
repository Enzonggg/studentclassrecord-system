package com.grading.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Attendance {
    private int id;
    private int studentId;
    private int classId;
    private LocalDate date;
    private String status;  // "Present", "Absent", "Late", "Excused"
    private String remarks;
    private LocalDateTime recordedAt;
    
    public Attendance() {
    }
    
    public Attendance(int id, int studentId, int classId, LocalDate date, String status, String remarks, LocalDateTime recordedAt) {
        this.id = id;
        this.studentId = studentId;
        this.classId = classId;
        this.date = date;
        this.status = status;
        this.remarks = remarks;
        this.recordedAt = recordedAt;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public int getClassId() {
        return classId;
    }
    
    public void setClassId(int classId) {
        this.classId = classId;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }
    
    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
