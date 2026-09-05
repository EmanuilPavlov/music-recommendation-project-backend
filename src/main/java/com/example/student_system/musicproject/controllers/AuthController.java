package com.example.student_system.musicproject.controllers;

import com.example.student_system.musicproject.dto.auth.records.AuthResponseDTO;
import com.example.student_system.musicproject.dto.auth.records.LoginRequestDTO;
import com.example.student_system.musicproject.dto.auth.records.RegisterDTO;
import com.example.student_system.musicproject.entities.User;
import com.example.student_system.musicproject.repository.UserRepository;
import com.example.student_system.musicproject.services.interfaces.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/singin")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) throws FirebaseAuthException {
        FirebaseToken firebaseToken = authService.verifyToken(request.token());
        User user = userRepository.findByFirebaseUid(firebaseToken.getUid());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/google-signin")
    public ResponseEntity<?> googleSignIn(@RequestBody LoginRequestDTO request) {
        try {
            if (request.token() == null || request.token().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Token is required"));
            }

            FirebaseToken firebaseToken = authService.verifyToken(request.token());
            System.out.println("Token verified for UID: " + firebaseToken.getUid());

            User user = userRepository.findByFirebaseUid(firebaseToken.getUid());
            if (user == null) {
                user = authService.createLocalUserFromFirebaseToken(
                        firebaseToken,
                        request.email() != null ? request.email() : firebaseToken.getEmail()
                );
                System.out.println("Created new user: " + user.getEmail());
            } else {
                System.out.println("Found existing user: " + user.getEmail());
            }

            AuthResponseDTO response = AuthResponseDTO.builder()
                    .firebaseUid(user.getFirebaseUid())
                    .email(user.getEmail())
                    .build();

            return ResponseEntity.ok(response);

        } catch (FirebaseAuthException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @PostMapping("/register")
    public String register(@RequestBody RegisterDTO request) throws FirebaseAuthException {
        return authService.register(request);
    }

    @PostMapping("/logout/{uid}")
    public String logout(@PathVariable String uid) throws FirebaseAuthException {
        authService.logout(uid);
        return "User logged out (tokens revoked)";
    }
}
