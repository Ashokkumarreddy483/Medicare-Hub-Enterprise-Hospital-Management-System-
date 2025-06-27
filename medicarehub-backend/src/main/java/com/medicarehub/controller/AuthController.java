package com.medicarehub.controller;

import com.medicarehub.dto.JwtResponseDto;
import com.medicarehub.dto.LoginRequestDto;
import com.medicarehub.dto.MessageResponseDto;
import com.medicarehub.dto.SignupRequestDto;
// import com.medicarehub.entity.User; // Not used directly here
import com.medicarehub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Keep this for method-level annotations if needed

// @CrossOrigin(origins = "*", maxAge = 3600) // REMOVE THIS LINE
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        JwtResponseDto jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDto signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(new MessageResponseDto("User registered successfully!"));
    }
}