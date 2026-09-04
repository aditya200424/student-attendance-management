package com.attendance.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Student entity. Stored in a HashMap<String studentId, Student> inside
 * StudentRepository, giving O(1) average lookup/insert/delete by studentId.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student {
    private String studentId;      // HashMap key
    private String rollNumber;
    private String name;
    private String email;
    private String passwordHash;   // SHA-256
    private String department;
    private String semester;
    private String section;
    private String phone;
    private String profilePhotoUrl;
    private Role role = Role.STUDENT;

    public Student() {}

    public Student(String studentId, String rollNumber, String name, String email, String passwordHash,
                   String department, String semester, String section) {
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.department = department;
        this.semester = semester;
        this.section = section;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
