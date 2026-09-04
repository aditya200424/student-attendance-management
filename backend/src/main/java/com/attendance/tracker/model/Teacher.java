package com.attendance.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Teacher entity, stored in HashMap<String teacherId, Teacher> -> O(1) lookup.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Teacher {
    private String teacherId;
    private String name;
    private String email;
    private String passwordHash;
    private String department;
    private List<String> subjectIds = new ArrayList<>();
    private List<String> assignedClasses = new ArrayList<>();
    private Role role = Role.TEACHER;

    public Teacher() {}

    public Teacher(String teacherId, String name, String email, String passwordHash, String department) {
        this.teacherId = teacherId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.department = department;
    }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public List<String> getSubjectIds() { return subjectIds; }
    public void setSubjectIds(List<String> subjectIds) { this.subjectIds = subjectIds; }
    public List<String> getAssignedClasses() { return assignedClasses; }
    public void setAssignedClasses(List<String> assignedClasses) { this.assignedClasses = assignedClasses; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
