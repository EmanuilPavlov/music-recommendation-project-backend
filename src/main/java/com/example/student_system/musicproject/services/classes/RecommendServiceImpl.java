package com.example.student_system.musicproject.services.classes;

import com.example.student_system.musicproject.dto.music_response.classes.MedicalConditionRecommendationResponse;
import com.example.student_system.musicproject.dto.music_response.classes.MoodRecommendationResponse;
import com.example.student_system.musicproject.dto.music_response.classes.SongRecommendationResponse;
import com.example.student_system.musicproject.agents.BridgeAgent;
import com.example.student_system.musicproject.services.interfaces.RecommendService;
import com.example.student_system.musicproject.services.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendServiceImpl implements RecommendService {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public List<MoodRecommendationResponse> recommendByUserMood(
            String userMood,
            Integer limit,
            String firebaseUid) {

        return requestRecommendations(
                "MoodAgent",
                userMood,
                limit,
                firebaseUid,
                MoodRecommendationResponse.class
        );
    }

    public List<MedicalConditionRecommendationResponse> recommendByCondition(
            String condition,
            Integer limit,
            String firebaseUid) {

        return requestRecommendations(
                "MedicalConditionAgent",
                condition ,
                limit,
                firebaseUid,
                MedicalConditionRecommendationResponse.class
        );
    }


    private <T extends SongRecommendationResponse> List<T> requestRecommendations(
            String targetAgent,
            String label,
            Integer limit,
            String firebaseUid,
            Class<T> responseType) {

        List<T> results = new ArrayList<>();

        try {
            CompletableFuture<String> future =
                    BridgeAgent.sendToAgent(targetAgent, label, limit);

            String response = future.get(20, TimeUnit.SECONDS);

            if (response != null
                    && !response.isEmpty()
                    && !response.equals("NO_RESULTS")) {

                T[] songs = objectMapper.readValue(
                        response,
                        objectMapper.getTypeFactory()
                                .constructArrayType(responseType)
                );

                results.addAll(Arrays.asList(songs));

                userService.saveSearch(
                        firebaseUid,
                        label,
                        limit
                );
            }

        } catch (Exception e) {
            log.error(
                    "Error communicating with BridgeAgent. " +
                            "targetAgent={}, message={}",
                    targetAgent,
                    label,
                    e)
            ;
        }

        return results;
    }
}