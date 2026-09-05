package com.example.student_system.musicproject.services.interfaces;

import com.example.student_system.musicproject.dto.auth.records.RegisterDTO;
import com.example.student_system.musicproject.entities.User;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public interface AuthService {
    FirebaseToken verifyToken(String token) throws FirebaseAuthException;
    String register(RegisterDTO registerDTO) throws FirebaseAuthException;
    User createLocalUserFromFirebaseToken(FirebaseToken firebaseToken, String email);
    void logout(String uid) throws FirebaseAuthException;
}
