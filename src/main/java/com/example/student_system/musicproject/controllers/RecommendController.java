package com.example.student_system.musicproject.controllers;

import com.example.student_system.musicproject.dto.music_response.classes.MedicalConditionRecommendationResponse;
import com.example.student_system.musicproject.dto.user_request.records.MedicalConditionRecommendationRequest;
import com.example.student_system.musicproject.dto.user_request.records.MoodRecommendationRequest;
import com.example.student_system.musicproject.dto.music_response.classes.MoodRecommendationResponse;
import com.example.student_system.musicproject.services.interfaces.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @PostMapping("/by-mood")
    public ResponseEntity<List<MoodRecommendationResponse>> getByMood(
            @RequestBody MoodRecommendationRequest request) {

        Integer limit = request.limit() != null ? request.limit() : 10;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("⚠️ No authenticated user found");
            return ResponseEntity.status(401).body(null);
        }

        String firebaseUid = (String) auth.getPrincipal();
        String firebaseEmail = (String) auth.getDetails();

        log.info("👤 User: {} ({}) requested mood: {}", firebaseEmail, firebaseUid, request.mood());

        List<MoodRecommendationResponse> songs = recommendService.recommendByUserMood(
                request.mood(),
                limit,
                firebaseUid
        );

        log.info("✅ Returning {} songs for user {}", songs.size(), firebaseUid);
        return ResponseEntity.ok(songs);
    }

    @PostMapping("/by-medicalCondition")
    public ResponseEntity<List<MedicalConditionRecommendationResponse>> getByCondition(@RequestBody MedicalConditionRecommendationRequest request) {

        Integer limit = request.limit() != null ? request.limit() : 10;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.warn("⚠️ No authenticated user found");
            return ResponseEntity.status(401).body(null);
        }

        String firebaseUid = (String) auth.getPrincipal();
        String firebaseEmail = (String) auth.getDetails();

        log.info("👤 User: {} ({}) requested condition: {}", firebaseEmail, firebaseUid, request.medicalCondition());

        List<MedicalConditionRecommendationResponse> songs = recommendService.recommendByCondition(
                request.medicalCondition(),
                limit,
                firebaseUid
        );

        log.info("✅ Returning {} songs for user {}", songs.size(), firebaseUid);
        return ResponseEntity.ok(songs);
    }
}