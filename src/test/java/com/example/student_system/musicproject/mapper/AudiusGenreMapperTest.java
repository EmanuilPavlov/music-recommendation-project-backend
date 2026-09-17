package com.example.student_system.musicproject.mapper;

import com.example.student_system.musicproject.mappers.AudiusGenreMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AudiusGenreMapper Unit Tests")
class AudiusGenreMapperTest {

    @Nested
    @DisplayName("Genre Mapping Tests")
    class GenreMappingTests {

        @Test
        @DisplayName("toAudiusGenre - Electronic Music")
        void testToAudiusGenre_ElectronicMusic() {
            String result =
                    AudiusGenreMapper.toAudiusGenre("ElectronicMusic");

            assertEquals("Electronic", result);
        }

        @Test
        @DisplayName("toAudiusGenre - Popular Music")
        void testToAudiusGenre_PopularMusic() {
            String result =
                    AudiusGenreMapper.toAudiusGenre("PopularMusic");

            assertEquals("Pop", result);
        }

        @Test
        @DisplayName("toAudiusGenre - Hip-Hop")
        void testToAudiusGenre_HipHop() {
            String result =
                    AudiusGenreMapper.toAudiusGenre("Hip-Hop");

            assertEquals("Hip-Hop/Rap", result);
        }

        @Test
        @DisplayName("toAudiusGenre - Lo-fi")
        void testToAudiusGenre_Lofi() {
            String result =
                    AudiusGenreMapper.toAudiusGenre("Lo-fi");

            assertEquals("Lo-Fi", result);
        }

        @Test
        @DisplayName("toAudiusGenre - Classical Music")
        void testToAudiusGenre_ClassicalMusic() {
            String result =
                    AudiusGenreMapper.toAudiusGenre("ClassicalMusic");

            assertEquals("Classical", result);
        }

        @Test
        @DisplayName("toAudiusGenre - Rock Music")
        void testToAudiusGenre_RockMusic() {
            String result =
                    AudiusGenreMapper.toAudiusGenre("RockMusic");

            assertEquals("Rock", result);
        }

        @Test
        @DisplayName("toAudiusGenre - World Music")
        void testToAudiusGenre_WorldMusic() {
            String result =
                    AudiusGenreMapper.toAudiusGenre("WorldMusic");

            assertEquals("World", result);
        }

        @Test
        @DisplayName("toAudiusGenre - Dubstep")
        void testToAudiusGenre_Dubstep() {
            String result =
                    AudiusGenreMapper.toAudiusGenre("Dubstep");

            assertEquals("Dubstep", result);
        }
    }

    @Nested
    @DisplayName("Invalid Genre Tests")
    class InvalidGenreTests {

        @Test
        @DisplayName("toAudiusGenre - Unknown Genre")
        void testToAudiusGenre_UnknownGenre() {
            String result =
                    AudiusGenreMapper.toAudiusGenre("UnknownGenre");

            assertNull(result);
        }

        @Test
        @DisplayName("toAudiusGenre - Null Genre")
        void testToAudiusGenre_NullGenre() {
            AudiusGenreMapper.toAudiusGenre(null);

            assertNull(null);
        }
    }
}