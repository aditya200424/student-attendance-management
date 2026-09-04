package com.attendance.tracker.controller;

import com.attendance.tracker.dto.ApiResponse;
import com.attendance.tracker.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(reportService.getDashboardSummary());
    }

    @GetMapping("/monthly")
    public ApiResponse<List<Map<String, Object>>> monthly(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(reportService.getAttendanceTrend(days));
    }

    @GetMapping("/department-wise")
    public ApiResponse<List<Map<String, Object>>> departmentWise() {
        return ApiResponse.ok(reportService.getDepartmentWiseAttendance());
    }

    @GetMapping("/student/{id}")
    public ApiResponse<Map<String, Object>> studentReport(@PathVariable String id) {
        return ApiResponse.ok(reportService.getStudentReport(id));
    }
}
