package com.example.student_system.musicproject.dto.auth.records;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank String token
) {
}
