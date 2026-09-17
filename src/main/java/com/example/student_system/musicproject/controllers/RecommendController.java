package com.example.student_system.musicproject.controllers;

import com.example.student_system.musicproject.dto.music_response.classes.MedicalConditionRecommendationResponse;
import com.example.student_system.musicproject.dto.user_request.records.MedicalConditionRecommendationRequest;
import com.example.student_system.musicproject.dto.user_request.records.MoodRecommendationRequest;
import com.example.student_system.musicproject.dto.music_response.classes.MoodRecommendationResponse;
import com.example.student_system.musicproject.services.interfaces.RecommendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @PostMapping("/by-mood")
    public ResponseEntity<List<MoodRecommendationResponse>> getByMood(@Valid @RequestBody MoodRecommendationRequest request, Authentication authentication) {
        String firebaseUid = (String) authentication.getPrincipal();

        List<MoodRecommendationResponse> songs =
                recommendService.recommendByUserMood(
                        request.mood(),
                        request.resolvedLimit(),
                        firebaseUid
                );

        return ResponseEntity.ok(songs);
    }

    @PostMapping("/by-medical-condition")
    public ResponseEntity<List<MedicalConditionRecommendationResponse>> getByCondition(@Valid @RequestBody MedicalConditionRecommendationRequest request, Authentication authentication) {
        String firebaseUid = (String) authentication.getPrincipal();

        List<MedicalConditionRecommendationResponse> songs =
                recommendService.recommendByCondition(
                        request.medicalCondition(),
                        request.resolvedLimit(),
                        firebaseUid
                );

        return ResponseEntity.ok(songs);
    }
}