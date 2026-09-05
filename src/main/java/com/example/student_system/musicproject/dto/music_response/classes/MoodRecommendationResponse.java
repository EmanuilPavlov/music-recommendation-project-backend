package com.example.student_system.musicproject.dto.music_response.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Component;

@Getter
@Component
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MoodRecommendationResponse
        extends SongRecommendationResponse {
    private String mood;
}