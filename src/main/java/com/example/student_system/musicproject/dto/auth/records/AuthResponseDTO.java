package com.example.student_system.musicproject.dto.auth.records;

import lombok.Builder;

@Builder
public record AuthResponseDTO(
        String email,
        String firebaseUid
) {
}
