package com.attendance.tracker.service;

import com.attendance.tracker.exception.BadRequestException;
import com.attendance.tracker.exception.ResourceNotFoundException;
import com.attendance.tracker.model.Student;
import com.attendance.tracker.repository.StudentRepository;
import com.attendance.tracker.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    /** O(1) average - HashMap direct lookup by primary key. */
    public Student getById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    public Student create(Student student) {
        if (student.getStudentId() == null || student.getStudentId().isBlank()) {
            student.setStudentId("STU" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (studentRepository.existsById(student.getStudentId())) {
            throw new BadRequestException("Student ID already exists");
        }
        // Default password = roll number, hashed with SHA-256; student should change it later.
        String rawPassword = student.getPasswordHash() != null ? student.getPasswordHash() : student.getRollNumber();
        student.setPasswordHash(PasswordUtil.hash(rawPassword));
        return studentRepository.save(student);
    }

    public Student update(String id, Student updated) {
        Student existing = getById(id);
        existing.setName(updated.getName() != null ? updated.getName() : existing.getName());
        existing.setEmail(updated.getEmail() != null ? updated.getEmail() : existing.getEmail());
        existing.setDepartment(updated.getDepartment() != null ? updated.getDepartment() : existing.getDepartment());
        existing.setSemester(updated.getSemester() != null ? updated.getSemester() : existing.getSemester());
        existing.setSection(updated.getSection() != null ? updated.getSection() : existing.getSection());
        existing.setPhone(updated.getPhone() != null ? updated.getPhone() : existing.getPhone());
        existing.setRollNumber(updated.getRollNumber() != null ? updated.getRollNumber() : existing.getRollNumber());
        if (updated.getProfilePhotoUrl() != null) existing.setProfilePhotoUrl(updated.getProfilePhotoUrl());
        return studentRepository.save(existing);
    }

    public void delete(String id) {
        if (!studentRepository.deleteById(id)) {
            throw new ResourceNotFoundException("Student not found: " + id);
        }
    }

    /**
     * Instant search across id, roll number, name, department, semester, section.
     * Exact-key fields (id/roll) go through O(1) HashMap lookup first; if the
     * query isn't an exact key match, falls back to an O(n) filtered scan over
     * name/department/semester/section (still fast for typical class sizes).
     */
    public List<Student> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        String q = query.trim().toLowerCase();

        // O(1) fast paths
        var byId = studentRepository.findById(query.trim());
        if (byId.isPresent()) return List.of(byId.get());
        var byRoll = studentRepository.findByRollNumber(query.trim());
        if (byRoll.isPresent()) return List.of(byRoll.get());

        // Fallback: filtered scan for partial/fuzzy matches
        return studentRepository.findAll().stream()
                .filter(s ->
                        (s.getName() != null && s.getName().toLowerCase().contains(q)) ||
                        (s.getDepartment() != null && s.getDepartment().toLowerCase().contains(q)) ||
                        (s.getSemester() != null && s.getSemester().toLowerCase().contains(q)) ||
                        (s.getSection() != null && s.getSection().toLowerCase().contains(q)) ||
                        (s.getRollNumber() != null && s.getRollNumber().toLowerCase().contains(q)) ||
                        (s.getStudentId() != null && s.getStudentId().toLowerCase().contains(q))
                )
                .collect(Collectors.toList());
    }

    public List<Student> findByClass(String department, String semester, String section) {
        return studentRepository.findAll().stream()
                .filter(s -> (department == null || department.equalsIgnoreCase(s.getDepartment())))
                .filter(s -> (semester == null || semester.equalsIgnoreCase(s.getSemester())))
                .filter(s -> (section == null || section.equalsIgnoreCase(s.getSection())))
                .collect(Collectors.toList());
    }
}
