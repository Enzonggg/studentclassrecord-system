package com.grading.util;

import com.grading.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public void clearSession() {
        currentUser = null;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return isLoggedIn() && currentUser.getRole() == User.Role.ADMIN;
    }
    
    public boolean isTeacher() {
        return isLoggedIn() && currentUser.getRole() == User.Role.TEACHER;
    }
    
    public boolean hasStudentInfo() {
        return isLoggedIn() && currentUser.getStudentInfo() != null && !currentUser.getStudentInfo().isEmpty();
    }
}
