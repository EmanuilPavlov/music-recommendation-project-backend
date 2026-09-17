package com.example.student_system.musicproject.services;

import com.example.student_system.musicproject.agents.BridgeAgent;
import com.example.student_system.musicproject.dto.music_response.classes.MedicalConditionRecommendationResponse;
import com.example.student_system.musicproject.dto.music_response.classes.MoodRecommendationResponse;
import com.example.student_system.musicproject.exceptions.RecommendationServiceException;
import com.example.student_system.musicproject.exceptions.RecommendationTimeoutException;
import com.example.student_system.musicproject.exceptions.ResourceNotFoundException;
import com.example.student_system.musicproject.services.classes.RecommendServiceImpl;
import com.example.student_system.musicproject.services.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendServiceImpl Unit Tests")
class RecommendServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private CompletableFuture<String> future;

    private RecommendServiceImpl recommendService;

    @BeforeEach
    void setUp() {
        recommendService = new RecommendServiceImpl(
                userService,
                new ObjectMapper()
        );
    }

    @Nested
    @DisplayName("Mood Recommendation Tests")
    class MoodRecommendationTests {

        @Test
        @DisplayName("recommendByUserMood - Success")
        void testRecommendByUserMood_Success() throws Exception {
            String jsonResponse = """
                    [
                      {
                        "title": "Test Song",
                        "artist": "Test Artist",
                        "artwork": "artwork",
                        "bpm": 120,
                        "audioUrl": "audio-url",
                        "duration": 180.0,
                        "mood": "Happy"
                      }
                    ]
                    """;

            when(future.get(20, TimeUnit.SECONDS))
                    .thenReturn(jsonResponse);

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MoodAgent",
                                        "Happy",
                                        10
                                )
                        )
                        .thenReturn(future);

                List<MoodRecommendationResponse> results =
                        recommendService.recommendByUserMood(
                                "Happy",
                                10,
                                "firebase-uid"
                        );

                assertNotNull(results);
                assertEquals(1, results.size());
                assertEquals("Test Song", results.get(0).getTitle());
                assertEquals("Test Artist", results.get(0).getArtist());
                assertEquals("artwork", results.get(0).getArtwork());
                assertEquals(120, results.get(0).getBpm());
                assertEquals("audio-url", results.get(0).getAudioUrl());
                assertEquals(180.0, results.get(0).getDuration());
                assertEquals("Happy", results.get(0).getMood());

                verify(userService).saveSearch(
                        "firebase-uid",
                        "Happy",
                        10
                );
            }
        }

        @Test
        @DisplayName("recommendByUserMood - No Results")
        void testRecommendByUserMood_NoResults() throws Exception {
            when(future.get(20, TimeUnit.SECONDS))
                    .thenReturn("NO_RESULTS");

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MoodAgent",
                                        "Happy",
                                        10
                                )
                        )
                        .thenReturn(future);

                List<MoodRecommendationResponse> results =
                        recommendService.recommendByUserMood(
                                "Happy",
                                10,
                                "firebase-uid"
                        );

                assertNotNull(results);
                assertTrue(results.isEmpty());

                verify(userService, never())
                        .saveSearch(
                                anyString(),
                                anyString(),
                                anyInt()
                        );
            }
        }

        @Test
        @DisplayName("recommendByUserMood - No Match")
        void testRecommendByUserMood_NoMatch() throws Exception {
            when(future.get(20, TimeUnit.SECONDS))
                    .thenReturn("NO_MATCH");

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MoodAgent",
                                        "UnknownMood",
                                        10
                                )
                        )
                        .thenReturn(future);

                assertThrows(
                        ResourceNotFoundException.class,
                        () -> recommendService.recommendByUserMood(
                                "UnknownMood",
                                10,
                                "firebase-uid"
                        )
                );

                verify(userService, never())
                        .saveSearch(
                                anyString(),
                                anyString(),
                                anyInt()
                        );
            }
        }

        @Test
        @DisplayName("recommendByUserMood - Agent Error")
        void testRecommendByUserMood_AgentError() throws Exception {
            when(future.get(20, TimeUnit.SECONDS))
                    .thenReturn("ERROR: Audius unavailable");

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MoodAgent",
                                        "Happy",
                                        10
                                )
                        )
                        .thenReturn(future);

                RecommendationServiceException exception =
                        assertThrows(
                                RecommendationServiceException.class,
                                () -> recommendService.recommendByUserMood(
                                        "Happy",
                                        10,
                                        "firebase-uid"
                                )
                        );

                assertEquals(
                        "Audius unavailable",
                        exception.getMessage()
                );

                verify(userService, never())
                        .saveSearch(
                                anyString(),
                                anyString(),
                                anyInt()
                        );
            }
        }

        @Test
        @DisplayName("recommendByUserMood - Timeout")
        void testRecommendByUserMood_Timeout() throws Exception {
            when(future.get(20, TimeUnit.SECONDS))
                    .thenThrow(new TimeoutException());

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MoodAgent",
                                        "Happy",
                                        10
                                )
                        )
                        .thenReturn(future);

                assertThrows(
                        RecommendationTimeoutException.class,
                        () -> recommendService.recommendByUserMood(
                                "Happy",
                                10,
                                "firebase-uid"
                        )
                );

                verify(userService, never())
                        .saveSearch(
                                anyString(),
                                anyString(),
                                anyInt()
                        );
            }
        }
    }

    @Nested
    @DisplayName("Medical Condition Recommendation Tests")
    class MedicalConditionRecommendationTests {

        @Test
        @DisplayName("recommendByCondition - Success")
        void testRecommendByCondition_Success() throws Exception {
            String jsonResponse = """
                    [
                      {
                        "title": "Calm Song",
                        "artist": "Test Artist",
                        "artwork": "artwork",
                        "bpm": 80,
                        "audioUrl": "audio-url",
                        "duration": 200.0,
                        "condition": "Anxiety",
                        "matchedEffect": "Stress Reduction",
                        "relatedSymptoms": [
                          "Stress",
                          "Restlessness"
                        ]
                      }
                    ]
                    """;

            when(future.get(20, TimeUnit.SECONDS))
                    .thenReturn(jsonResponse);

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MedicalConditionAgent",
                                        "Anxiety",
                                        10
                                )
                        )
                        .thenReturn(future);

                List<MedicalConditionRecommendationResponse> results =
                        recommendService.recommendByCondition(
                                "Anxiety",
                                10,
                                "firebase-uid"
                        );

                assertNotNull(results);
                assertEquals(1, results.size());
                assertEquals("Calm Song", results.get(0).getTitle());
                assertEquals("Test Artist", results.get(0).getArtist());
                assertEquals("artwork", results.get(0).getArtwork());
                assertEquals(80, results.get(0).getBpm());
                assertEquals("audio-url", results.get(0).getAudioUrl());
                assertEquals(200.0, results.get(0).getDuration());
                assertEquals("Anxiety", results.get(0).getCondition());
                assertEquals(
                        "Stress Reduction",
                        results.get(0).getMatchedEffect()
                );
                assertEquals(
                        2,
                        results.get(0).getRelatedSymptoms().size()
                );
                assertEquals(
                        "Stress",
                        results.get(0).getRelatedSymptoms().get(0)
                );
                assertEquals(
                        "Restlessness",
                        results.get(0).getRelatedSymptoms().get(1)
                );

                verify(userService).saveSearch(
                        "firebase-uid",
                        "Anxiety",
                        10
                );
            }
        }

        @Test
        @DisplayName("recommendByCondition - No Results")
        void testRecommendByCondition_NoResults() throws Exception {
            when(future.get(20, TimeUnit.SECONDS))
                    .thenReturn("NO_RESULTS");

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MedicalConditionAgent",
                                        "Anxiety",
                                        10
                                )
                        )
                        .thenReturn(future);

                List<MedicalConditionRecommendationResponse> results =
                        recommendService.recommendByCondition(
                                "Anxiety",
                                10,
                                "firebase-uid"
                        );

                assertNotNull(results);
                assertTrue(results.isEmpty());

                verify(userService, never())
                        .saveSearch(
                                anyString(),
                                anyString(),
                                anyInt()
                        );
            }
        }

        @Test
        @DisplayName("recommendByCondition - No Match")
        void testRecommendByCondition_NoMatch() throws Exception {
            when(future.get(20, TimeUnit.SECONDS))
                    .thenReturn("NO_MATCH");

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MedicalConditionAgent",
                                        "UnknownCondition",
                                        10
                                )
                        )
                        .thenReturn(future);

                assertThrows(
                        ResourceNotFoundException.class,
                        () -> recommendService.recommendByCondition(
                                "UnknownCondition",
                                10,
                                "firebase-uid"
                        )
                );

                verify(userService, never())
                        .saveSearch(
                                anyString(),
                                anyString(),
                                anyInt()
                        );
            }
        }

        @Test
        @DisplayName("recommendByCondition - Agent Error")
        void testRecommendByCondition_AgentError() throws Exception {
            when(future.get(20, TimeUnit.SECONDS))
                    .thenReturn("ERROR: Audius unavailable");

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MedicalConditionAgent",
                                        "Anxiety",
                                        10
                                )
                        )
                        .thenReturn(future);

                RecommendationServiceException exception =
                        assertThrows(
                                RecommendationServiceException.class,
                                () -> recommendService.recommendByCondition(
                                        "Anxiety",
                                        10,
                                        "firebase-uid"
                                )
                        );

                assertEquals(
                        "Audius unavailable",
                        exception.getMessage()
                );

                verify(userService, never())
                        .saveSearch(
                                anyString(),
                                anyString(),
                                anyInt()
                        );
            }
        }

        @Test
        @DisplayName("recommendByCondition - Timeout")
        void testRecommendByCondition_Timeout() throws Exception {
            when(future.get(20, TimeUnit.SECONDS))
                    .thenThrow(new TimeoutException());

            try (MockedStatic<BridgeAgent> bridgeAgent = mockStatic(BridgeAgent.class)) {
                bridgeAgent.when(() ->
                                BridgeAgent.sendToAgent(
                                        "MedicalConditionAgent",
                                        "Anxiety",
                                        10
                                )
                        )
                        .thenReturn(future);

                assertThrows(
                        RecommendationTimeoutException.class,
                        () -> recommendService.recommendByCondition(
                                "Anxiety",
                                10,
                                "firebase-uid"
                        )
                );

                verify(userService, never())
                        .saveSearch(
                                anyString(),
                                anyString(),
                                anyInt()
                        );
            }
        }
    }
}