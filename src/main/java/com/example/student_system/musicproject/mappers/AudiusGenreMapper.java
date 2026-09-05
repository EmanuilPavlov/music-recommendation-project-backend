package com.example.student_system.musicproject.mappers;

import java.util.Map;

public final class AudiusGenreMapper {

    private AudiusGenreMapper() {}

    private static final Map<String, String> ONTOLOGY_TO_AUDIUS = Map.ofEntries(
            // --- Electronic family ---
            Map.entry("Ambient", "Ambient"),
            Map.entry("Chillout", "Ambient"),
            Map.entry("Electro", "Electro"),
            Map.entry("ElectroPop", "Pop"),
            Map.entry("ElectronicMusic", "Electronic"),
            Map.entry("House", "House"),
            Map.entry("Techno", "Techno"),
            Map.entry("Trance", "Trance"),
            Map.entry("Trap", "Trap"),
            Map.entry("Vaporwave", "Vaporwave"),
            Map.entry("Dubste", "Dubstep"), // ontology class is literally named "Dubste"
            Map.entry("Hybrid", "Experimental"),

            // --- Rock family ---
            Map.entry("AlternativeRock", "Alternative"),
            Map.entry("HardRock", "Rock"),
            Map.entry("IndieRock", "Alternative"),
            Map.entry("ProgressiveRock", "Rock"),
            Map.entry("PunkRock", "Punk"),
            Map.entry("RockAndRoll", "Rock"),
            Map.entry("RockMusic", "Rock"),

            // --- Metal family ---
            Map.entry("BlackMetal", "Metal"),
            Map.entry("DeathMetal", "Metal"),
            Map.entry("HeavyMetal", "Metal"),
            Map.entry("Metal", "Metal"),
            Map.entry("ThrashMetal", "Metal"),

            // --- Blues family ---
            Map.entry("Blues", "Blues"),
            Map.entry("ChicagoBlues", "Blues"),
            Map.entry("DeltaBlues", "Blues"),
            Map.entry("TexasBlues", "Blues"),

            // --- Jazz / classical ---
            Map.entry("Jazz", "Jazz"),
            Map.entry("Bebop", "Jazz"),
            Map.entry("Swing", "Jazz"),
            Map.entry("ClassicalMusic", "Classical"),
            Map.entry("Baroque", "Classical"),
            Map.entry("Opera", "Classical"),
            Map.entry("Symphonic", "Classical"),

            // --- Folk / country ---
            Map.entry("FolkMusic", "Folk"),
            Map.entry("Neofolk", "Folk"),
            Map.entry("TraditionalFolk", "Folk"),
            Map.entry("Country", "Country"),

            // --- Pop family ---
            Map.entry("Pop", "Pop"),
            Map.entry("PopularMusic", "Pop"),
            Map.entry("DancePop", "Pop"),
            Map.entry("TeenPop", "Pop"),
            Map.entry("J-pop", "Pop"),
            Map.entry("K-pop", "Pop"),

            // --- Hip-Hop family ---
            Map.entry("Hip-Hop", "Hip-Hop/Rap"),
            Map.entry("OldSchoolHip-hop", "Hip-Hop/Rap"),
            Map.entry("Lo-fiHip-hop", "Lo-Fi"),

            // --- Lo-fi ---
            Map.entry("Lo-fi", "Lo-Fi"),

            // --- Latin / world ---
            Map.entry("LatinMusic", "Latin"),
            Map.entry("Bachata", "Latin"),
            Map.entry("Reggaeton", "Latin"),
            Map.entry("Salsa", "Latin"),
            Map.entry("Afrobeat", "World"),
            Map.entry("BalkanMusic", "World"),
            Map.entry("WorldMusic", "World"),

            // --- Soundtrack ---
            Map.entry("Soundtrack", "Soundtrack"),
            Map.entry("OriginalSoundtrack", "Soundtrack")
    );

    public static String toAudiusGenre(String ontologyGenre) {
        if (ontologyGenre == null) return null;
        return ONTOLOGY_TO_AUDIUS.get(ontologyGenre);
    }
}