package com.attendance.tracker.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    private String id;         // studentId / teacherId / adminId or email
    @NotBlank
    private String password;
    @NotBlank
    private String role;       // ADMIN, TEACHER, STUDENT

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
