package com.example.student_system.musicproject.repository;

import com.example.student_system.musicproject.entities.User;
import com.example.student_system.musicproject.entities.UserSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSearchHistoryRepository extends JpaRepository<UserSearchHistory, Long> {
    List<UserSearchHistory> findUserSearchHistoriesByUser(User user);
}
