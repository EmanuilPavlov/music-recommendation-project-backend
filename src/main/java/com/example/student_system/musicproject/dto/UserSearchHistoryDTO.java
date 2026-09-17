package com.example.student_system.musicproject.dto;

import java.time.LocalDateTime;

public record UserSearchHistoryDTO(
        String firebaseUid,
        String typeOfSearch,
        Integer limitRequested,
        LocalDateTime searchTimestamp
) {
}
