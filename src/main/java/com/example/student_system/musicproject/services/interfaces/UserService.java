package com.example.student_system.musicproject.services.interfaces;


import com.example.student_system.musicproject.dto.UserSearchHistoryDTO;

import java.util.List;

public interface UserService {
    void saveSearch(
            String firebaseUid,
            String mood,
            Integer limitRequested);

    List<UserSearchHistoryDTO> getSearchHistory(String firebaseUid);
}
