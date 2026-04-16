package com.nitinconstructions.service;


import com.nitinconstructions.dto.LoginResponse;
import com.nitinconstructions.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Value("${app.admin.password}")
    private String adminPassword;

    private final JwtUtil jwtUtil;

    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(String password) {
        if (password == null || !password.equals(adminPassword)) {
            throw new IllegalArgumentException("Incorrect password.");
        }
        String token = jwtUtil.generateToken();
        return new LoginResponse(token, jwtUtil.getExpirationMs());
    }
}
