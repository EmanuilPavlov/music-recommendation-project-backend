package com.example.student_system.musicproject.services.classes;

import com.example.student_system.musicproject.dto.BPMRange;
import com.example.student_system.musicproject.dto.SongData;
import com.example.student_system.musicproject.exceptions.RecommendationServiceException;
import com.example.student_system.musicproject.mappers.AudiusGenreMapper;
import com.example.student_system.musicproject.services.interfaces.AudiusService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;


@Slf4j
@Service
public class AudiusServiceImpl implements AudiusService {

    private static final String APP_NAME = "music_project";
    private static final List<String> ARTWORK_SIZE_PREFERENCE = List.of("480x480", "1000x1000", "150x150");
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.audius.co")
            .codecs(configurer ->
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)
            )
            .build();

    @Override
    public List<SongData> fetchSongs(
            List<String> genres,
            List<BPMRange> bpmRanges,
            Integer searchLimit
    ) {
        Map<String, SongData> songs = new LinkedHashMap<>();

        for (String genre : genres) {
            List<SongData> fetchedSongs = searchTracks(genre, Math.min(searchLimit * 5, 50));

            for (SongData song : fetchedSongs) {
                for (BPMRange bpmRange : bpmRanges) {
                    if (song.bpm() >= bpmRange.min() && song.bpm() < bpmRange.max()) {
                        songs.putIfAbsent(songKey(song), song);
                        break;
                    }
                }
            }
        }

        return new ArrayList<>(songs.values());
    }

    private String songKey(SongData song) {
        if (song.audioUrl() != null && !song.audioUrl().isBlank())
            return song.audioUrl();

        return song.artist() + "|" + song.title();
    }

    private List<SongData> searchTracks(
            String genre,
            int limit
    ) {
        String audiusGenre = AudiusGenreMapper.toAudiusGenre(genre);

        try {
            String response = webClient.get()
                    .uri(uriBuilder ->
                            uriBuilder
                                    .path("/v1/tracks/trending")
                                    .queryParam("app_name", APP_NAME)
                                    .queryParam("limit", limit)
                                    .queryParam("genre", audiusGenre)
                                    .build()
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);

            if (response == null)
                return List.of();


            JsonNode jsonNode = objectMapper.readTree(response);
            List<JsonNode> tracks = new ArrayList<>();

            if (jsonNode.has("data"))
                for (JsonNode track : jsonNode.get("data"))
                    tracks.add(track);



            return tracks.stream()
                    .map(this::toSongData)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            log.error("Error fetching tracks from Audius API", e);
            throw new RecommendationServiceException("Failed to fetch tracks from Audius");
        }
    }


    private SongData toSongData(JsonNode track) {
        String title = track.has("title") ? track.get("title").asText() : "Unknown";
        String artist = track.has("user") && track.get("user").has("name") ? track.get("user").get("name").asText() : "Unknown";
        int bpm = track.has("bpm") ? track.get("bpm").asInt() : 0;
        String trackId = track.has("id") ? track.get("id").asText() : "";
        String artwork = extractCoverArt(track);
        double duration = track.has("duration") ? track.get("duration").asDouble() : 0;

        if (bpm <= 0 || trackId.isEmpty()) return null;

        return new SongData(title, artist, artwork, bpm, trackId, duration);
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
}