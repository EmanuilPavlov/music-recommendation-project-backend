package com.example.student_system.musicproject.services.classes;

import com.example.student_system.musicproject.dto.auth.records.RegisterDTO;
import com.example.student_system.musicproject.entities.User;
import com.example.student_system.musicproject.mappers.UserMapper;
import com.example.student_system.musicproject.repository.UserRepository;
import com.example.student_system.musicproject.services.interfaces.AuthService;
import com.google.firebase.auth.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final FirebaseAuth firebaseAuth;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public FirebaseToken verifyToken(String token) throws FirebaseAuthException {
        return firebaseAuth.verifyIdToken(token);
    }

    public String register(RegisterDTO registerDTO) throws FirebaseAuthException {

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(registerDTO.email())
                .setPassword(registerDTO.password());

        UserRecord userRecord = firebaseAuth.createUser(request);

        userRepository.save(userMapper.toEntity(userRecord.getUid(), registerDTO));

        return userRecord.getUid();
    }

    public User createLocalUserFromFirebaseToken(FirebaseToken firebaseToken, String email) {
        User user = new User();
        user.setFirebaseUid(firebaseToken.getUid());
        user.setEmail(email != null ? email : firebaseToken.getEmail());

        return userRepository.save(user);
    }

    public void logout(String uid) throws FirebaseAuthException {
        firebaseAuth.revokeRefreshTokens(uid);
    }
}