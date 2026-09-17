package com.example.student_system.musicproject.services;

import com.example.student_system.musicproject.dto.SongData;
import com.example.student_system.musicproject.services.classes.AudiusServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AudiusServiceImpl Unit Tests")
class AudiusServiceImplTest {

    private AudiusServiceImpl audiusService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        audiusService = new AudiusServiceImpl();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Song Conversion Tests")
    class SongConversionTests {

        @Test
        @DisplayName("toSongData - Success")
        void testToSongData_Success() throws Exception {
            JsonNode track = objectMapper.readTree("""
                    {
                      "id": "track123",
                      "title": "Test Song",
                      "bpm": 120,
                      "duration": 180,
                      "user": {
                        "name": "Test Artist"
                      },
                      "artwork": {
                        "480x480": "cover.jpg"
                      }
                    }
                    """);

            Method method = AudiusServiceImpl.class
                    .getDeclaredMethod("toSongData", JsonNode.class);
            method.setAccessible(true);

            SongData result = (SongData) method.invoke(audiusService, track);

            assertNotNull(result);
            assertEquals("Test Song", result.title());
            assertEquals("Test Artist", result.artist());
            assertEquals(120, result.bpm());
            assertEquals("track123", result.audioUrl());
            assertEquals("cover.jpg", result.artwork());
            assertEquals(180, result.duration());
        }

        @Test
        @DisplayName("toSongData - Missing BPM")
        void testToSongData_MissingBpm() throws Exception {
            JsonNode track = objectMapper.readTree("""
                    {
                      "id": "track123",
                      "title": "Test Song",
                      "user": {
                        "name": "Test Artist"
                      }
                    }
                    """);

            Method method = AudiusServiceImpl.class
                    .getDeclaredMethod("toSongData", JsonNode.class);
            method.setAccessible(true);

            SongData result = (SongData) method.invoke(audiusService, track);

            assertNull(result);
        }

        @Test
        @DisplayName("toSongData - Missing Track ID")
        void testToSongData_MissingTrackId() throws Exception {
            JsonNode track = objectMapper.readTree("""
                    {
                      "title": "Test Song",
                      "bpm": 120,
                      "user": {
                        "name": "Test Artist"
                      }
                    }
                    """);

            Method method = AudiusServiceImpl.class
                    .getDeclaredMethod("toSongData", JsonNode.class);
            method.setAccessible(true);

            SongData result = (SongData) method.invoke(audiusService, track);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Artwork Tests")
    class ArtworkTests {

        @Test
        @DisplayName("extractCoverArt - Preferred Size")
        void testExtractCoverArt_PreferredSize() throws Exception {
            JsonNode track = objectMapper.readTree("""
                    {
                      "artwork": {
                        "150x150": "small.jpg",
                        "480x480": "medium.jpg",
                        "1000x1000": "large.jpg"
                      }
                    }
                    """);

            Method method = AudiusServiceImpl.class
                    .getDeclaredMethod("extractCoverArt", JsonNode.class);
            method.setAccessible(true);

            String result = (String) method.invoke(audiusService, track);

            assertEquals("medium.jpg", result);
        }

        @Test
        @DisplayName("extractCoverArt - Missing Artwork")
        void testExtractCoverArt_MissingArtwork() throws Exception {
            JsonNode track = objectMapper.readTree("""
                    {
                      "id": "track123"
                    }
                    """);

            Method method = AudiusServiceImpl.class
                    .getDeclaredMethod("extractCoverArt", JsonNode.class);
            method.setAccessible(true);

            String result = (String) method.invoke(audiusService, track);

            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Song Key Tests")
    class SongKeyTests {

        @Test
        @DisplayName("songKey - Uses Audio URL")
        void testSongKey_UsesAudioUrl() throws Exception {
            SongData song = new SongData(
                    "Test Song",
                    "Test Artist",
                    "cover.jpg",
                    120,
                    "track123",
                    180
            );

            Method method = AudiusServiceImpl.class
                    .getDeclaredMethod("songKey", SongData.class);
            method.setAccessible(true);

            String result = (String) method.invoke(audiusService, song);

            assertEquals("track123", result);
        }

        @Test
        @DisplayName("songKey - Uses Artist And Title")
        void testSongKey_UsesArtistAndTitle() throws Exception {
            SongData song = new SongData(
                    "Test Song",
                    "Test Artist",
                    "cover.jpg",
                    120,
                    "",
                    180
            );

            Method method = AudiusServiceImpl.class
                    .getDeclaredMethod("songKey", SongData.class);
            method.setAccessible(true);

            String result = (String) method.invoke(audiusService, song);

            assertEquals("Test Artist|Test Song", result);
        }
    }
}