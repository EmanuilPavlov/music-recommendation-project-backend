package com.example.student_system.musicproject.controllers;

import com.example.student_system.musicproject.dto.auth.records.AuthResponseDTO;
import com.example.student_system.musicproject.dto.auth.records.AuthRequestDTO;
import com.example.student_system.musicproject.entities.User;
import com.example.student_system.musicproject.services.interfaces.AuthService;

import org.springframework.security.core.Authentication;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> login(
            @Valid @RequestBody AuthRequestDTO request) throws FirebaseAuthException {

        FirebaseToken firebaseToken = authService.verifyToken(request.token());
        User user = authService.findOrCreateLocalUser(firebaseToken);

        AuthResponseDTO response =
                AuthResponseDTO.builder()
                        .id(user.getFirebaseUid())
                        .username(user.getEmail())
                        .role("USER")
                        .email(user.getEmail())
                        .firebaseUid(user.getFirebaseUid())
                        .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) throws FirebaseAuthException {
        String firebaseUid = authentication.getPrincipal().toString();
        authService.logout(firebaseUid);
        return ResponseEntity.noContent().build();
    }
}
