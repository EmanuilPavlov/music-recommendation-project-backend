package com.example.student_system.musicproject.dto.music_response.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class SongRecommendationResponse {
    protected Long id;
    protected String title;
    protected String artwork;
    protected String artist;
    protected int bpm;
    protected String audioUrl;
    protected double duration;
}