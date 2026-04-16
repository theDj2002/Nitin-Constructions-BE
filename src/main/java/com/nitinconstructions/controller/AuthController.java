package com.nitinconstructions.controller;


import com.nitinconstructions.dto.ApiResponse;
import com.nitinconstructions.dto.LoginRequest;
import com.nitinconstructions.dto.LoginResponse;
import com.nitinconstructions.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest req) {
        try {
            LoginResponse response = authService.login(req.password());
            return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /api/auth/verify  — protected by JwtAuthFilter
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verify(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponse.ok("Token is valid.", "admin"));
        }
        return ResponseEntity.status(401).body(ApiResponse.error("Invalid token."));
    }
}
