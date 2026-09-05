package com.example.student_system.musicproject.services.classes;

import com.example.student_system.musicproject.dto.SongData;
import com.example.student_system.musicproject.mappers.AudiusGenreMapper;
import com.example.student_system.musicproject.services.interfaces.AudiusService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AudiusServiceImpl implements AudiusService {

    private static final String APP_NAME = "music_project";
    private static final List<String> ARTWORK_SIZE_PREFERENCE = List.of("480x480", "1000x1000", "150x150");


    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.audius.co")
            .codecs(configurer ->
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10 MB
            )
            .build();

    public List<SongData> searchTracks(String genre, Integer bpmMin, Integer bpmMax, int limit) {
        String audiusGenre = AudiusGenreMapper.toAudiusGenre(genre);

        List<JsonNode> popularTracks = fetchTrendingTracks(audiusGenre, bpmMin, bpmMax, limit);
        List<JsonNode> recentTracks = fetchTrendingTracks(audiusGenre, bpmMin, bpmMax, limit);

        if (popularTracks.isEmpty() && recentTracks.isEmpty()) {
            log.warn("No trending tracks found for genre '{}', falling back to search", audiusGenre);
            popularTracks = fetchRawTracks(audiusGenre, bpmMin, bpmMax, limit);
            recentTracks = fetchRawTracks(audiusGenre, bpmMin, bpmMax, limit);
        }

        List<JsonNode> merged = interleaveDeduped(popularTracks, recentTracks, limit);

        List<SongData> songs = new ArrayList<>();
        for (JsonNode track : merged) {
            SongData song = toSongData(track);
            if (song != null && song.duration() > 30) {
                songs.add(song);
            }
        }
        return songs;
    }

    private List<JsonNode> fetchTrendingTracks(String genre, Integer bpmMin, Integer bpmMax, int limit) {
        List<JsonNode> tracks = new ArrayList<>();

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/v1/tracks/trending")
                                .queryParam("app_name", APP_NAME)
                                .queryParam("limit", limit);

                        if (genre != null && !genre.isEmpty()) {
                            uriBuilder.queryParam("genre", genre);
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.has("data")) {
                    for (JsonNode track : jsonNode.get("data")) {
                        if (bpmMin != null || bpmMax != null) {
                            double bpm = track.has("bpm") ? track.get("bpm").asDouble() : 0;
                            if (bpmMin != null && bpm < bpmMin) continue;
                            if (bpmMax != null && bpm > bpmMax) continue;
                        }
                        tracks.add(track);
                    }
                }
            }

            log.info("Fetched {} trending tracks for genre '{}'", tracks.size(), genre);

        } catch (Exception e) {
            log.error("Error fetching trending tracks for genre '{}' : {}", genre, e.getMessage());
        }

        return tracks;
    }

    private List<JsonNode> interleaveDeduped(List<JsonNode> first, List<JsonNode> second, int limit) {
        Map<String, JsonNode> result = new LinkedHashMap<>();
        int i = 0, j = 0;

        while (result.size() < limit && (i < first.size() || j < second.size())) {
            if (i < first.size()) {
                JsonNode track = first.get(i++);
                String id = track.has("id") ? track.get("id").asText() : null;
                if (id != null) result.putIfAbsent(id, track);
            }
            if (result.size() >= limit) break;
            if (j < second.size()) {
                JsonNode track = second.get(j++);
                String id = track.has("id") ? track.get("id").asText() : null;
                if (id != null) result.putIfAbsent(id, track);
            }
        }

        return new ArrayList<>(result.values());
    }

    private List<JsonNode> fetchRawTracks(String genre, Integer bpmMin, Integer bpmMax, int limit) {
        List<JsonNode> tracks = new ArrayList<>();

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/v1/tracks/search")
                                .queryParam("app_name", APP_NAME)
                                .queryParam("limit", limit);

                        if (genre != null) uriBuilder.queryParam("genre", genre);
                        if (bpmMin != null) uriBuilder.queryParam("bpm_min", bpmMin);
                        if (bpmMax != null) uriBuilder.queryParam("bpm_max", bpmMax);

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.has("data")) {
                    for (JsonNode track : jsonNode.get("data")) {
                        tracks.add(track);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error searching Audius for genre '{}' : {}", genre, e.getMessage());
        }

        return tracks;
    }

    private SongData toSongData(JsonNode track) {
        String title = track.has("title") ? track.get("title").asText() : "Unknown";
        String artist = track.has("user") && track.get("user").has("name") ? track.get("user").get("name").asText() : "Unknown";
        double bpm = track.has("bpm") ? track.get("bpm").asDouble() : 0;
        String trackId = track.has("id") ? track.get("id").asText() : "";
        String artwork = extractCoverArt(track);
        double duration = track.has("duration") ? track.get("duration").asDouble() : 0;

        if (bpm <= 0 || trackId.isEmpty()) return null;

        return new SongData(title, artist, artwork, (int) bpm, buildStreamUrl(trackId), duration);
    }

    private String extractCoverArt(JsonNode track) {
        if (!track.has("artwork")) return "";
        JsonNode artwork = track.get("artwork");

        for (String size : ARTWORK_SIZE_PREFERENCE) {
            if (artwork.has(size)) return artwork.get(size).asText();
        }

        Iterator<String> fieldNames = artwork.fieldNames();
        if (fieldNames.hasNext()) return artwork.get(fieldNames.next()).asText();

        return "";
    }

    private String buildStreamUrl(String trackId) {
        return "https://api.audius.co/v1/tracks/" + trackId + "/stream?app_name=" + APP_NAME;
    }
}