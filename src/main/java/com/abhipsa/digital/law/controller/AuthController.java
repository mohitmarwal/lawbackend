package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.dto.ChangePasswordRequest;
import com.abhipsa.digital.law.dto.LoginRequest;
import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.repository.UserRepository;
import com.abhipsa.digital.law.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repository;

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest request) {

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with email: " + request.getEmail()
                ));

        if (!user.getPassword().equals(request.getPassword())) {
            // Fix: Wrap the error string into a Map to match the Map<String, String> generic type
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("error", "Invalid credentials");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED) // Uses standard 401 status enum
                    .body(errorBody);
        }

        String token = jwtService.generateToken(user.getEmail());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("token", token);

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/account/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            // Find user by email (you can also use SecurityContext if JWT is used)
            User user = repository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Verify current password
            if (!user.getPassword().equals(request.getCurrentPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Current password is incorrect"));
            }

            // Update password
            user.setPassword(request.getNewPassword());
            repository.save(user);

            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
