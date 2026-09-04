package com.attendance.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AttendanceMarkRequest {
    @NotBlank
    private String subjectId;
    @NotBlank
    private String teacherId;
    @NotBlank
    private String date;
    @NotBlank
    private String department;
    @NotBlank
    private String semester;
    @NotBlank
    private String section;
    private List<StudentStatus> records;

    public static class StudentStatus {
        private String studentId;
        private String status; // PRESENT, ABSENT, LATE, LEAVE

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public List<StudentStatus> getRecords() { return records; }
    public void setRecords(List<StudentStatus> records) { this.records = records; }
}
