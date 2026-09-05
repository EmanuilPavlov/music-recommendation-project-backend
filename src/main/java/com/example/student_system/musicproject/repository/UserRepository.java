package com.example.student_system.musicproject.repository;

import com.example.student_system.musicproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
        User findByFirebaseUid(String firebaseUid);
}
