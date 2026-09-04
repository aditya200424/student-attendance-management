package com.attendance.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Admin {
    private String adminId;
    private String name;
    private String email;
    private String passwordHash;
    private Role role = Role.ADMIN;

    public Admin() {}

    public Admin(String adminId, String name, String email, String passwordHash) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
