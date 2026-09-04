package com.attendance.tracker.controller;

import com.attendance.tracker.dto.ApiResponse;
import com.attendance.tracker.dto.AttendanceMarkRequest;
import com.attendance.tracker.model.Attendance;
import com.attendance.tracker.service.AttendanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/mark")
    public ApiResponse<List<Attendance>> mark(@RequestBody AttendanceMarkRequest request) {
        return ApiResponse.ok("Attendance marked", attendanceService.markAttendance(request));
    }

    @PutMapping("/update")
    public ApiResponse<Attendance> update(@RequestParam String studentId, @RequestParam String subjectId,
                                           @RequestParam String date, @RequestParam String status) {
        return ApiResponse.ok("Attendance updated", attendanceService.updateAttendance(studentId, subjectId, date, status));
    }

    @DeleteMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam String studentId, @RequestParam String subjectId, @RequestParam String date) {
        attendanceService.deleteAttendance(studentId, subjectId, date);
        return ApiResponse.ok("Attendance record deleted", null);
    }

    @PostMapping("/undo")
    public ApiResponse<Attendance> undo() {
        return attendanceService.undoLast()
                .map(a -> ApiResponse.ok("Last attendance action undone", a))
                .orElse(ApiResponse.error("Nothing to undo"));
    }

    @GetMapping("/student/{id}")
    public ApiResponse<List<Attendance>> byStudent(@PathVariable String id) {
        return ApiResponse.ok(attendanceService.getStudentAttendance(id));
    }

    @GetMapping("/student/{id}/percentage")
    public ApiResponse<Double> percentage(@PathVariable String id) {
        return ApiResponse.ok(attendanceService.calculatePercentage(id));
    }

    @GetMapping("/class")
    public ApiResponse<List<Attendance>> classAttendance(@RequestParam String subjectId, @RequestParam String date) {
        return ApiResponse.ok(attendanceService.getClassAttendance(subjectId, date));
    }

    @GetMapping("/leaderboard")
    public ApiResponse<List<Map<String, Object>>> leaderboard(@RequestParam(defaultValue = "10") int topN) {
        return ApiResponse.ok(attendanceService.getLeaderboard(topN));
    }
}
