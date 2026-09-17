package com.example.student_system.musicproject.ontology;

import com.example.student_system.musicproject.dto.BPMRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MusicOntology Unit Tests")
class MusicOntologyTest {

    private MusicOntology musicOntology;

    @BeforeEach
    void setUp() {
        musicOntology = new MusicOntology();
    }

    @Nested
    @DisplayName("Tempo Tests")
    class TempoTests {

        @Test
        @DisplayName("tempoBPMRanges - Loads Slow Tempo")
        void testTempoBPMRanges_LoadsSlowTempo() {
            BPMRange result =
                    musicOntology.getTempoBPMRanges()
                            .get("SlowTempoSong");

            assertNotNull(result);
            assertEquals(40, result.min());
            assertEquals(76, result.max());
        }

        @Test
        @DisplayName("tempoBPMRanges - Loads Medium Tempo")
        void testTempoBPMRanges_LoadsMediumTempo() {
            BPMRange result =
                    musicOntology.getTempoBPMRanges()
                            .get("MediumTempoSong");

            assertNotNull(result);
            assertEquals(76, result.min());
            assertEquals(120, result.max());
        }

        @Test
        @DisplayName("tempoBPMRanges - Loads Fast Tempo")
        void testTempoBPMRanges_LoadsFastTempo() {
            BPMRange result =
                    musicOntology.getTempoBPMRanges()
                            .get("FastTempoSong");

            assertNotNull(result);
            assertEquals(120, result.min());
            assertEquals(200, result.max());
        }
    }

    @Nested
    @DisplayName("Mood Tests")
    class MoodTests {

        @Test
        @DisplayName("getGenresForMood - Happy")
        void testGetGenresForMood_Happy() {
            List<String> result =
                    musicOntology.getGenresForMood("Happy");

            assertNotNull(result);
            assertFalse(result.isEmpty());

            assertEquals(
                    Set.of(
                            "ElectronicMusic",
                            "Hip-Hop",
                            "PopularMusic",
                            "Soundtrack",
                            "WorldMusic"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getGenresForMood - Sad")
        void testGetGenresForMood_Sad() {
            List<String> result =
                    musicOntology.getGenresForMood("Sad");

            assertNotNull(result);
            assertFalse(result.isEmpty());

            assertEquals(
                    Set.of(
                            "Blues",
                            "ClassicalMusic",
                            "FolkMusic",
                            "Jazz"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getGenresForMood - Angry")
        void testGetGenresForMood_Angry() {
            List<String> result =
                    musicOntology.getGenresForMood("Angry");

            assertNotNull(result);
            assertFalse(result.isEmpty());

            assertEquals(
                    Set.of(
                            "Metal",
                            "RockMusic"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getGenresForMood - Romantic")
        void testGetGenresForMood_Romantic() {
            List<String> result =
                    musicOntology.getGenresForMood("Romantic");

            assertNotNull(result);
            assertFalse(result.isEmpty());

            assertEquals(
                    Set.of(
                            "ClassicalMusic",
                            "Jazz",
                            "PopularMusic",
                            "WorldMusic"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getBPMRangesForMood - Happy")
        void testGetBPMRangesForMood_Happy() {
            List<BPMRange> result =
                    musicOntology.getBPMRangesForMood("Happy");

            assertNotNull(result);
            assertEquals(2, result.size());

            assertTrue(result.contains(new BPMRange(76, 120)));
            assertTrue(result.contains(new BPMRange(120, 200)));
        }

        @Test
        @DisplayName("getGenresForMood - Unknown Mood")
        void testGetGenresForMood_UnknownMood() {
            List<String> result =
                    musicOntology.getGenresForMood("UnknownMood");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Medical Condition Tests")
    class MedicalConditionTests {

        @Test
        @DisplayName("getEffectsForCondition - ADHD")
        void testGetEffectsForCondition_ADHD() {
            List<String> result =
                    musicOntology.getEffectsForCondition("ADHD");

            assertNotNull(result);

            assertEquals(
                    Set.of(
                            "ConcentrationBoost",
                            "FocusEnhancementEffect"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getEffectsForCondition - Anxiety")
        void testGetEffectsForCondition_Anxiety() {
            List<String> result =
                    musicOntology.getEffectsForCondition("Anxiety");

            assertNotNull(result);

            assertEquals(
                    Set.of(
                            "AnxietyReductionEffect",
                            "RelaxationEffect"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getEffectsForCondition - Dementia")
        void testGetEffectsForCondition_Dementia() {
            List<String> result =
                    musicOntology.getEffectsForCondition("Dementia");

            assertNotNull(result);

            assertEquals(
                    Set.of(
                            "MemorySupport",
                            "RelaxationEffect"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getEffectsForCondition - Depression")
        void testGetEffectsForCondition_Depression() {
            List<String> result =
                    musicOntology.getEffectsForCondition("Depression");

            assertNotNull(result);

            assertEquals(
                    Set.of(
                            "EnergyBoost",
                            "MoodImprovementEffect",
                            "RelaxationEffect"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getEffectsForCondition - Dyslexia")
        void testGetEffectsForCondition_Dyslexia() {
            List<String> result =
                    musicOntology.getEffectsForCondition("Dyslexia");

            assertNotNull(result);

            assertEquals(
                    Set.of(
                            "ConcentrationBoost",
                            "FocusEnhancementEffect"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getEffectsForCondition - Insomnia")
        void testGetEffectsForCondition_Insomnia() {
            List<String> result =
                    musicOntology.getEffectsForCondition("Insomnia");

            assertNotNull(result);

            assertEquals(
                    Set.of("SleepInductionEffect"),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getSymptomsForCondition - ADHD")
        void testGetSymptomsForCondition_ADHD() {
            List<String> result =
                    musicOntology.getSymptomsForCondition("ADHD");

            assertNotNull(result);
            assertFalse(result.isEmpty());

            assertTrue(result.contains("BrainFog"));
            assertTrue(result.contains("Distractibility"));
            assertTrue(result.contains("PoorConcentration"));
        }

        @Test
        @DisplayName("getEffectsForCondition - Unknown Condition")
        void testGetEffectsForCondition_UnknownCondition() {
            List<String> result =
                    musicOntology.getEffectsForCondition("UnknownCondition");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Effect Tests")
    class EffectTests {

        @Test
        @DisplayName("getGenresForEffect - Sleep Induction")
        void testGetGenresForEffect_SleepInduction() {
            List<String> result =
                    musicOntology.getGenresForEffect("SleepInductionEffect");

            assertNotNull(result);

            assertEquals(
                    Set.of(
                            "Ambient",
                            "Chillout"
                    ),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getTemposForEffect - Sleep Induction")
        void testGetTemposForEffect_SleepInduction() {
            List<String> result =
                    musicOntology.getTemposForEffect("SleepInductionEffect");

            assertNotNull(result);

            assertEquals(
                    Set.of("SlowTempoSong"),
                    Set.copyOf(result)
            );
        }

        @Test
        @DisplayName("getBPMRangesForEffect - Sleep Induction")
        void testGetBPMRangesForEffect_SleepInduction() {
            List<BPMRange> result =
                    musicOntology.getBPMRangesForEffect("SleepInductionEffect");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(
                    new BPMRange(40, 76),
                    result.get(0)
            );
        }

        @Test
        @DisplayName("getGenresForEffect - Unknown Effect")
        void testGetGenresForEffect_UnknownEffect() {
            List<String> result =
                    musicOntology.getGenresForEffect("UnknownEffect");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}