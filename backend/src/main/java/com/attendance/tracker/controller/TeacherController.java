package com.attendance.tracker.controller;

import com.attendance.tracker.dto.ApiResponse;
import com.attendance.tracker.model.Teacher;
import com.attendance.tracker.service.TeacherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ApiResponse<List<Teacher>> getAll() {
        return ApiResponse.ok(teacherService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Teacher> getById(@PathVariable String id) {
        return ApiResponse.ok(teacherService.getById(id));
    }

    @PostMapping
    public ApiResponse<Teacher> create(@RequestBody Teacher teacher) {
        return ApiResponse.ok("Teacher created", teacherService.create(teacher));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        teacherService.delete(id);
        return ApiResponse.ok("Teacher removed", null);
    }
}
