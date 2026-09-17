package com.example.student_system.musicproject.mappers;

import java.util.Map;

public final class AudiusGenreMapper {

    private AudiusGenreMapper() {}

    private static final Map<String, String> ONTOLOGY_TO_AUDIUS = Map.<String, String>ofEntries(
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
            Map.entry("Dubstep", "Dubstep"),
            Map.entry("Hybrid", "Experimental"),
            Map.entry("AlternativeRock", "Alternative"),
            Map.entry("HardRock", "Rock"),
            Map.entry("IndieRock", "Alternative"),
            Map.entry("ProgressiveRock", "Rock"),
            Map.entry("PunkRock", "Punk"),
            Map.entry("RockAndRoll", "Rock"),
            Map.entry("RockMusic", "Rock"),
            Map.entry("BlackMetal", "Metal"),
            Map.entry("DeathMetal", "Metal"),
            Map.entry("HeavyMetal", "Metal"),
            Map.entry("Metal", "Metal"),
            Map.entry("ThrashMetal", "Metal"),
            Map.entry("Blues", "Blues"),
            Map.entry("ChicagoBlues", "Blues"),
            Map.entry("DeltaBlues", "Blues"),
            Map.entry("TexasBlues", "Blues"),
            Map.entry("Jazz", "Jazz"),
            Map.entry("Bebop", "Jazz"),
            Map.entry("Swing", "Jazz"),
            Map.entry("ClassicalMusic", "Classical"),
            Map.entry("Baroque", "Classical"),
            Map.entry("Opera", "Classical"),
            Map.entry("Symphonic", "Classical"),
            Map.entry("FolkMusic", "Folk"),
            Map.entry("Neofolk", "Folk"),
            Map.entry("TraditionalFolk", "Folk"),
            Map.entry("Country", "Country"),
            Map.entry("Pop", "Pop"),
            Map.entry("PopularMusic", "Pop"),
            Map.entry("DancePop", "Pop"),
            Map.entry("TeenPop", "Pop"),
            Map.entry("J-pop", "Pop"),
            Map.entry("K-pop", "Pop"),
            Map.entry("Hip-Hop", "Hip-Hop/Rap"),
            Map.entry("OldSchoolHip-hop", "Hip-Hop/Rap"),
            Map.entry("Lo-fiHip-hop", "Lo-Fi"),
            Map.entry("Lo-fi", "Lo-Fi"),
            Map.entry("LatinMusic", "Latin"),
            Map.entry("Bachata", "Latin"),
            Map.entry("Reggaeton", "Latin"),
            Map.entry("Salsa", "Latin"),
            Map.entry("Afrobeat", "World"),
            Map.entry("BalkanMusic", "World"),
            Map.entry("WorldMusic", "World"),
            Map.entry("Soundtrack", "Soundtrack"),
            Map.entry("OriginalSoundtrack", "Soundtrack")
    );

    public static String toAudiusGenre(String ontologyGenre) {
        if (ontologyGenre == null) return null;
        return ONTOLOGY_TO_AUDIUS.get(ontologyGenre);
    }
}