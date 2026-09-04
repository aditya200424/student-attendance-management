package com.attendance.tracker.controller;

import com.attendance.tracker.dto.ApiResponse;
import com.attendance.tracker.model.Student;
import com.attendance.tracker.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ApiResponse<List<Student>> getAll(@RequestParam(required = false) String search,
                                              @RequestParam(required = false) String department,
                                              @RequestParam(required = false) String semester,
                                              @RequestParam(required = false) String section) {
        if (search != null && !search.isBlank()) {
            return ApiResponse.ok(studentService.search(search));
        }
        if (department != null || semester != null || section != null) {
            return ApiResponse.ok(studentService.findByClass(department, semester, section));
        }
        return ApiResponse.ok(studentService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Student> getById(@PathVariable String id) {
        return ApiResponse.ok(studentService.getById(id));
    }

    @PostMapping
    public ApiResponse<Student> create(@RequestBody Student student) {
        return ApiResponse.ok("Student created", studentService.create(student));
    }

    @PutMapping("/{id}")
    public ApiResponse<Student> update(@PathVariable String id, @RequestBody Student student) {
        return ApiResponse.ok("Student updated", studentService.update(id, student));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        studentService.delete(id);
        return ApiResponse.ok("Student deleted", null);
    }
}
