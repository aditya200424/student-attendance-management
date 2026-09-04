package com.attendance.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single attendance record for one student, one subject, one date.
 * Stored in HashMap<String attendanceId, Attendance> in AttendanceRepository
 * where attendanceId = studentId + "_" + subjectId + "_" + date (composite key)
 * giving O(1) average lookup for "has this student been marked today for this subject".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attendance {
    private String attendanceId;   // composite HashMap key
    private String studentId;
    private String subjectId;
    private String teacherId;
    private String date;           // yyyy-MM-dd
    private AttendanceStatus status;
    private String department;
    private String semester;
    private String section;
    private long markedAt;

    public Attendance() {}

    public Attendance(String attendanceId, String studentId, String subjectId, String teacherId,
                       String date, AttendanceStatus status, String department, String semester, String section) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.date = date;
        this.status = status;
        this.department = department;
        this.semester = semester;
        this.section = section;
        this.markedAt = System.currentTimeMillis();
    }

    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String attendanceId) { this.attendanceId = attendanceId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public long getMarkedAt() { return markedAt; }
    public void setMarkedAt(long markedAt) { this.markedAt = markedAt; }
}
