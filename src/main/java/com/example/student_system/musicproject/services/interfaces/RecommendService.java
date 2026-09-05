package com.example.student_system.musicproject.services.interfaces;

import com.example.student_system.musicproject.dto.music_response.classes.MedicalConditionRecommendationResponse;
import com.example.student_system.musicproject.dto.music_response.classes.MoodRecommendationResponse;

import java.util.List;

public interface RecommendService {
    List<MoodRecommendationResponse> recommendByUserMood(
            String userMood,
            Integer limit,
            String firebaseUid);

    List<MedicalConditionRecommendationResponse> recommendByCondition(
            String condition,
            Integer limit,
            String firebaseUid);

}
