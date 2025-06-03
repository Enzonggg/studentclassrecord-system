package com.grading.model;

import java.time.LocalDateTime;

public class User {
    
    public enum Role {
        ADMIN, TEACHER
    }
    
    private int id;
    private String username;
    private String password;
    private Role role;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
    private String studentInfo; // Renamed from studentId to hold general student information

    // Constructors
    public User() {}

    public User(int id, String username, String password, Role role, String fullName, String email, LocalDateTime createdAt, String studentInfo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
        this.studentInfo = studentInfo;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentInfo() {
        return studentInfo;
    }

    public void setStudentInfo(String studentInfo) {
        this.studentInfo = studentInfo;
    }
    
    @Override
    public String toString() {
        return fullName;
    }
}
