package com.attendance.tracker.controller;

import com.attendance.tracker.dto.ApiResponse;
import com.attendance.tracker.dto.LoginRequest;
import com.attendance.tracker.dto.LoginResponse;
import com.attendance.tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok("Login successful", response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        // Stateless JWT - client simply discards the token.
        return ApiResponse.ok("Logged out successfully", null);
    }
}
