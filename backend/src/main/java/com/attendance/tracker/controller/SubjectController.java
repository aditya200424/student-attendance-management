package com.attendance.tracker.controller;

import com.attendance.tracker.dto.ApiResponse;
import com.attendance.tracker.model.Subject;
import com.attendance.tracker.repository.SubjectRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectRepository subjectRepository;

    public SubjectController(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @GetMapping
    public ApiResponse<List<Subject>> getAll() {
        return ApiResponse.ok(subjectRepository.findAll());
    }

    @PostMapping
    public ApiResponse<Subject> create(@RequestBody Subject subject) {
        if (subject.getSubjectId() == null || subject.getSubjectId().isBlank()) {
            subject.setSubjectId("SUB" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return ApiResponse.ok("Subject created", subjectRepository.save(subject));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        subjectRepository.deleteById(id);
        return ApiResponse.ok("Subject removed", null);
    }
}
