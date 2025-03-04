package com.securitydemo.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api")
public class AuthController {
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        if (("user".equals(username) && "password".equals(password)) ||
                ("admin".equals(username) && "adminpass".equals(password))) {
            String role = "user".equals(username) ? "USER" : "ADMIN";
            String token = Base64.getEncoder().encodeToString(
                    ("{\"sub\": \"" + username + "\", \"role\": \"ROLE_" + role + "\"}").getBytes()
            );
            return "Bearer " + token;
        }
        throw new RuntimeException("Invalid credentials");
    }
}
