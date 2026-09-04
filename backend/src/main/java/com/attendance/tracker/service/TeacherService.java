package com.attendance.tracker.service;

import com.attendance.tracker.exception.BadRequestException;
import com.attendance.tracker.exception.ResourceNotFoundException;
import com.attendance.tracker.model.Teacher;
import com.attendance.tracker.repository.TeacherRepository;
import com.attendance.tracker.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public List<Teacher> getAll() {
        return teacherRepository.findAll();
    }

    public Teacher getById(String id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + id));
    }

    public Teacher create(Teacher teacher) {
        if (teacher.getTeacherId() == null || teacher.getTeacherId().isBlank()) {
            teacher.setTeacherId("TCH" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (teacherRepository.findById(teacher.getTeacherId()).isPresent()) {
            throw new BadRequestException("Teacher ID already exists");
        }
        String rawPassword = teacher.getPasswordHash() != null ? teacher.getPasswordHash() : "teacher@123";
        teacher.setPasswordHash(PasswordUtil.hash(rawPassword));
        return teacherRepository.save(teacher);
    }

    public void delete(String id) {
        if (!teacherRepository.deleteById(id)) {
            throw new ResourceNotFoundException("Teacher not found: " + id);
        }
    }
}
