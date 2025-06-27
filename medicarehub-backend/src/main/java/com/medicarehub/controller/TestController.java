package com.medicarehub.controller; // Adjust package if different

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String testEndpoint() {
        return "Backend is running!";
    }
}