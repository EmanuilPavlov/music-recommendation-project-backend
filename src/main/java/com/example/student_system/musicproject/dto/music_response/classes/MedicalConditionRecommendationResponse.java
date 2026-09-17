package com.example.student_system.musicproject.dto.music_response.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class MedicalConditionRecommendationResponse extends SongRecommendationResponse {
    private String condition;
    private String matchedEffect;
    private List<String> relatedSymptoms;
}
