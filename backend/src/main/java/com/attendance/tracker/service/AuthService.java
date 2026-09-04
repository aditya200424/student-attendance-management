package com.attendance.tracker.service;

import com.attendance.tracker.dto.LoginRequest;
import com.attendance.tracker.dto.LoginResponse;
import com.attendance.tracker.exception.UnauthorizedException;
import com.attendance.tracker.model.Admin;
import com.attendance.tracker.model.Student;
import com.attendance.tracker.model.Teacher;
import com.attendance.tracker.repository.AdminRepository;
import com.attendance.tracker.repository.StudentRepository;
import com.attendance.tracker.repository.TeacherRepository;
import com.attendance.tracker.security.JwtUtil;
import com.attendance.tracker.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authenticates against the in-memory HashMap-backed repositories.
 * Login lookup is O(1) average because it goes through the HashMap index
 * (by id or by email) in each repository - no linear scan required.
 */
@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;

    public AuthService(StudentRepository studentRepository, TeacherRepository teacherRepository,
                        AdminRepository adminRepository, JwtUtil jwtUtil) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        String role = request.getRole() == null ? "" : request.getRole().toUpperCase();

        switch (role) {
            case "ADMIN": {
                Optional<Admin> admin = adminRepository.findById(request.getId());
                if (admin.isEmpty()) admin = adminRepository.findByEmail(request.getId());
                Admin a = admin.orElseThrow(() -> new UnauthorizedException("Invalid admin credentials"));
                if (!PasswordUtil.matches(request.getPassword(), a.getPasswordHash())) {
                    throw new UnauthorizedException("Invalid admin credentials");
                }
                String token = jwtUtil.generateToken(a.getAdminId(), "ADMIN");
                return new LoginResponse(token, a.getAdminId(), a.getName(), "ADMIN");
            }
            case "TEACHER": {
                Optional<Teacher> teacher = teacherRepository.findById(request.getId());
                if (teacher.isEmpty()) teacher = teacherRepository.findByEmail(request.getId());
                Teacher t = teacher.orElseThrow(() -> new UnauthorizedException("Invalid teacher credentials"));
                if (!PasswordUtil.matches(request.getPassword(), t.getPasswordHash())) {
                    throw new UnauthorizedException("Invalid teacher credentials");
                }
                String token = jwtUtil.generateToken(t.getTeacherId(), "TEACHER");
                return new LoginResponse(token, t.getTeacherId(), t.getName(), "TEACHER");
            }
            case "STUDENT": {
                Optional<Student> student = studentRepository.findById(request.getId());
                if (student.isEmpty()) student = studentRepository.findByEmail(request.getId());
                if (student.isEmpty()) student = studentRepository.findByRollNumber(request.getId());
                Student s = student.orElseThrow(() -> new UnauthorizedException("Invalid student credentials"));
                if (!PasswordUtil.matches(request.getPassword(), s.getPasswordHash())) {
                    throw new UnauthorizedException("Invalid student credentials");
                }
                String token = jwtUtil.generateToken(s.getStudentId(), "STUDENT");
                return new LoginResponse(token, s.getStudentId(), s.getName(), "STUDENT");
            }
            default:
                throw new UnauthorizedException("Unknown role: " + request.getRole());
        }
    }
}
