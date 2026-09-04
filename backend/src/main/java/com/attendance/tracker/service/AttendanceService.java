package com.attendance.tracker.service;

import com.attendance.tracker.dto.AttendanceMarkRequest;
import com.attendance.tracker.exception.BadRequestException;
import com.attendance.tracker.model.Attendance;
import com.attendance.tracker.model.AttendanceStatus;
import com.attendance.tracker.repository.AttendanceRepository;
import com.attendance.tracker.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AttendanceService {

    private static final double LOW_ATTENDANCE_THRESHOLD = 75.0;

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Marks attendance for a whole class in one go. Each record is written
     * into the HashMap-backed repository with an O(1) average insert, keyed
     * by studentId_subjectId_date so re-marking the same day just overwrites
     * (idempotent) instead of creating duplicates.
     */
    public List<Attendance> markAttendance(AttendanceMarkRequest request) {
        if (request.getRecords() == null || request.getRecords().isEmpty()) {
            throw new BadRequestException("No attendance records supplied");
        }
        List<Attendance> saved = new ArrayList<>();
        for (AttendanceMarkRequest.StudentStatus rec : request.getRecords()) {
            String key = AttendanceRepository.buildKey(rec.getStudentId(), request.getSubjectId(), request.getDate());
            AttendanceStatus status;
            try {
                status = AttendanceStatus.valueOf(rec.getStatus().toUpperCase());
            } catch (Exception e) {
                throw new BadRequestException("Invalid attendance status: " + rec.getStatus());
            }
            Attendance attendance = new Attendance(key, rec.getStudentId(), request.getSubjectId(), request.getTeacherId(),
                    request.getDate(), status, request.getDepartment(), request.getSemester(), request.getSection());
            saved.add(attendanceRepository.save(attendance));
        }
        return saved;
    }

    public Attendance updateAttendance(String studentId, String subjectId, String date, String statusStr) {
        String key = AttendanceRepository.buildKey(studentId, subjectId, date);
        Attendance existing = attendanceRepository.findById(key)
                .orElseThrow(() -> new BadRequestException("No attendance record found to update"));
        try {
            existing.setStatus(AttendanceStatus.valueOf(statusStr.toUpperCase()));
        } catch (Exception e) {
            throw new BadRequestException("Invalid attendance status: " + statusStr);
        }
        return attendanceRepository.save(existing);
    }

    public boolean deleteAttendance(String studentId, String subjectId, String date) {
        String key = AttendanceRepository.buildKey(studentId, subjectId, date);
        return attendanceRepository.deleteById(key);
    }

    /** O(1) average bucket fetch via HashMap, then O(k) scan of that student's own records. */
    public List<Attendance> getStudentAttendance(String studentId) {
        return attendanceRepository.findByStudent(studentId);
    }

    /** Undo the most recently marked attendance record - O(1) Stack pop. */
    public Optional<Attendance> undoLast() {
        return attendanceRepository.undoLast();
    }

    public double calculatePercentage(String studentId) {
        List<Attendance> records = attendanceRepository.findByStudent(studentId);
        if (records.isEmpty()) return 0.0;
        long presentCount = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                .count();
        long countable = records.stream().filter(a -> a.getStatus() != AttendanceStatus.HOLIDAY).count();
        if (countable == 0) return 0.0;
        return Math.round((presentCount * 10000.0) / countable) / 100.0;
    }

    public double calculatePercentageForSubject(String studentId, String subjectId) {
        List<Attendance> records = attendanceRepository.findByStudent(studentId).stream()
                .filter(a -> a.getSubjectId().equals(subjectId))
                .toList();
        if (records.isEmpty()) return 0.0;
        long presentCount = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                .count();
        return Math.round((presentCount * 10000.0) / records.size()) / 100.0;
    }

    public boolean isLowAttendance(String studentId) {
        return calculatePercentage(studentId) < LOW_ATTENDANCE_THRESHOLD;
    }

    /**
     * Leaderboard of top-N students by attendance percentage, built with a
     * PriorityQueue (max-heap via reversed comparator). Inserting into the
     * heap is O(log n); draining n students in ranked order is O(n log n),
     * far better than sorting the entire student list with a comparator
     * from scratch by hand for large n, and demonstrates heap-based ranking.
     */
    public List<Map<String, Object>> getLeaderboard(int topN) {
        PriorityQueue<Map.Entry<String, Double>> maxHeap = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getValue(), a.getValue())
        );
        for (var student : studentRepository.findAll()) {
            double pct = calculatePercentage(student.getStudentId());
            maxHeap.offer(Map.entry(student.getStudentId(), pct));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        while (!maxHeap.isEmpty() && result.size() < topN) {
            var entry = maxHeap.poll();
            var student = studentRepository.findById(entry.getKey()).orElse(null);
            if (student == null) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", rank++);
            row.put("studentId", student.getStudentId());
            row.put("name", student.getName());
            row.put("department", student.getDepartment());
            row.put("percentage", entry.getValue());
            result.add(row);
        }
        return result;
    }

    public List<Attendance> getClassAttendance(String subjectId, String date) {
        return attendanceRepository.findAll().stream()
                .filter(a -> a.getSubjectId().equals(subjectId) && a.getDate().equals(date))
                .toList();
    }

    public void requestCorrection(Attendance attendance) {
        attendanceRepository.enqueuePendingRequest(attendance);
    }

    public List<Attendance> getPendingRequests() {
        return attendanceRepository.peekAllPending();
    }
}
