package com.example.student_system.musicproject.dto.user_request.records;

import lombok.Builder;

@Builder
public record MedicalConditionRecommendationRequest(
        String medicalCondition,
        Integer limit
) {
}
