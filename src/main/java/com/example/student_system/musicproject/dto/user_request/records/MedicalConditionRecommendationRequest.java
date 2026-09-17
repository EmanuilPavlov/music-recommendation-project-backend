package com.example.student_system.musicproject.dto.user_request.records;

import com.example.student_system.musicproject.dto.user_request.interfaces.LimitedRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record MedicalConditionRecommendationRequest(
        @NotBlank String medicalCondition,
        @Min(10) @Max(50) Integer limit
) implements LimitedRequest {
}
