package com.example.student_system.musicproject.dto.auth.records;

public record LoginRequestDTO(
        String token,
        String email
) {
}
