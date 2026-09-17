package com.example.student_system.musicproject.services.classes;

import com.example.student_system.musicproject.entities.User;
import com.example.student_system.musicproject.repository.UserRepository;
import com.example.student_system.musicproject.services.interfaces.AuthService;
import com.google.firebase.auth.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    @Override
    public FirebaseToken verifyToken(String token) throws FirebaseAuthException {
        return firebaseAuth.verifyIdToken(token);
    }

    @Override
    public User findOrCreateLocalUser(FirebaseToken firebaseToken) {
        String firebaseUid = firebaseToken.getUid();

        User existingUser = userRepository.findByFirebaseUid(firebaseUid);
        if (existingUser != null) return existingUser;

        User newUser = new User();
        newUser.setFirebaseUid(firebaseUid);
        newUser.setEmail(firebaseToken.getEmail());
        return userRepository.save(newUser);
    }

    @Override
    public void logout(String uid) throws FirebaseAuthException {
        firebaseAuth.revokeRefreshTokens(uid);
    }
}