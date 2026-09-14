package com.basiltech.sipafin.controller;

import com.basiltech.sipafin.dto.UserDtos;
import com.basiltech.sipafin.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public UserDtos.AuthResponse login(@Valid @RequestBody UserDtos.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public UserDtos.AuthResponse register(@Valid @RequestBody UserDtos.RegisterUserRequest request) {
        return authService.register(request);
    }

    @GetMapping("/profile")
    public UserDtos.UserResponse profile(Authentication authentication) {
        return authService.profile(authentication.getName());
    }
}