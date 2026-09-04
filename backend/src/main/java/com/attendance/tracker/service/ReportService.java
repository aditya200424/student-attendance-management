package com.attendance.tracker.service;

import com.attendance.tracker.model.Attendance;
import com.attendance.tracker.model.AttendanceStatus;
import com.attendance.tracker.model.Student;
import com.attendance.tracker.repository.AttendanceRepository;
import com.attendance.tracker.repository.StudentRepository;
import com.attendance.tracker.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AttendanceService attendanceService;

    public ReportService(AttendanceRepository attendanceRepository, StudentRepository studentRepository,
                          TeacherRepository teacherRepository, AttendanceService attendanceService) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.attendanceService = attendanceService;
    }

    /** Summary numbers for the admin dashboard cards. */
    public Map<String, Object> getDashboardSummary() {
        List<Student> students = studentRepository.findAll();
        String today = LocalDate.now().toString();

        List<Attendance> todaysRecords = attendanceRepository.findAll().stream()
                .filter(a -> a.getDate().equals(today))
                .toList();

        long presentToday = todaysRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                .count();
        long absentToday = todaysRecords.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();

        double presentPct = todaysRecords.isEmpty() ? 0.0 : Math.round((presentToday * 10000.0) / todaysRecords.size()) / 100.0;
        double absentPct = todaysRecords.isEmpty() ? 0.0 : Math.round((absentToday * 10000.0) / todaysRecords.size()) / 100.0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalStudents", students.size());
        summary.put("totalTeachers", teacherRepository.findAll().size());
        summary.put("todaysAttendanceCount", todaysRecords.size());
        summary.put("presentPercentage", presentPct);
        summary.put("absentPercentage", absentPct);
        summary.put("totalClasses", students.stream()
                .map(s -> s.getDepartment() + "-" + s.getSemester() + "-" + s.getSection())
                .distinct().count());
        return summary;
    }

    /** Attendance trend over the last N days (for the line chart). */
    public List<Map<String, Object>> getAttendanceTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate cursor = LocalDate.now().minusDays(days - 1L);
        Map<String, List<Attendance>> byDate = attendanceRepository.findAll().stream()
                .collect(Collectors.groupingBy(Attendance::getDate));

        for (int i = 0; i < days; i++) {
            String dateStr = cursor.toString();
            List<Attendance> dayRecords = byDate.getOrDefault(dateStr, List.of());
            long present = dayRecords.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                    .count();
            double pct = dayRecords.isEmpty() ? 0.0 : Math.round((present * 10000.0) / dayRecords.size()) / 100.0;
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", dateStr);
            point.put("percentage", pct);
            trend.add(point);
            cursor = cursor.plusDays(1);
        }
        return trend;
    }

    /** Department-wise average attendance percentage (for bar chart). */
    public List<Map<String, Object>> getDepartmentWiseAttendance() {
        Map<String, List<Student>> byDept = studentRepository.findAll().stream()
                .collect(Collectors.groupingBy(s -> s.getDepartment() == null ? "Unknown" : s.getDepartment()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : byDept.entrySet()) {
            double avg = entry.getValue().stream()
                    .mapToDouble(s -> attendanceService.calculatePercentage(s.getStudentId()))
                    .average().orElse(0.0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("department", entry.getKey());
            row.put("averagePercentage", Math.round(avg * 100.0) / 100.0);
            result.add(row);
        }
        return result;
    }

    /** Report for a single student: overall %, subject-wise breakdown, monthly trend. */
    public Map<String, Object> getStudentReport(String studentId) {
        List<Attendance> records = attendanceRepository.findByStudent(studentId);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("studentId", studentId);
        report.put("overallPercentage", attendanceService.calculatePercentage(studentId));
        report.put("totalRecords", records.size());
        report.put("lowAttendance", attendanceService.isLowAttendance(studentId));

        Map<String, List<Attendance>> bySubject = records.stream()
                .collect(Collectors.groupingBy(Attendance::getSubjectId));
        List<Map<String, Object>> subjectWise = new ArrayList<>();
        for (var entry : bySubject.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("subjectId", entry.getKey());
            row.put("percentage", attendanceService.calculatePercentageForSubject(studentId, entry.getKey()));
            subjectWise.add(row);
        }
        report.put("subjectWise", subjectWise);
        return report;
    }
}
