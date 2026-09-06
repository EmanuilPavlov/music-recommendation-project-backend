package com.example.student_system.musicproject.agents;

import com.example.student_system.musicproject.dto.SongData;
import com.example.student_system.musicproject.dto.music_response.classes.MoodRecommendationResponse;
import com.example.student_system.musicproject.ontology.MusicOntology;
import com.example.student_system.musicproject.services.interfaces.AudiusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MoodAgent extends Agent {

    private MusicOntology ontology;
    private final AudiusService audiusService;
    private ObjectMapper objectMapper;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args == null || args.length == 0 || !(args[0] instanceof ObjectMapper)) {
            log.error("MoodAgent started without a valid ObjectMapper argument, terminating agent");
            doDelete();
            return;
        }
        this.objectMapper = (ObjectMapper) args[0];

        ontology = new MusicOntology();

        log.info("MoodAgent is ready to process requests");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    processRequest(msg);
                } else {
                    block();
                }
            }
        });
    }

    private void processRequest(ACLMessage msg) {
        try {
            String userMood = msg.getContent();
            int limit = Integer.parseInt(msg.getUserDefinedParameter(BridgeAgent.LIMIT_PARAM));
            log.info("Processing mood: {}, limit: {}", userMood, limit);

            List<String> genres = ontology.getGenresForMood(userMood);

            if (genres.isEmpty()) {
                log.error("No genres found for mood: {}", userMood);
                sendResponse(msg, "NO_RESULTS");
                return;
            }

            List<MusicOntology.BPMRange> ranges = ontology.getBPMRangesForMood(userMood);

            List<SongData> allSongs = new ArrayList<>();
            for (String genre : genres) {
                if (ranges.isEmpty()) {
                    allSongs.addAll(audiusService.searchTracks(genre, null, null, 100));
                    continue;
                }
                for (MusicOntology.BPMRange range : ranges) {
                    allSongs.addAll(audiusService.searchTracks(genre, range.min(), range.max() - 1, 100));
                }
            }

            List<SongData> filteredSongs = new ArrayList<>();
            for (SongData song : allSongs) {
                if (filteredSongs.size() >= limit) break;
                if (ontology.isBPMInMoodRange(song.bpm(), userMood)) filteredSongs.add(song);
            }

            List<MoodRecommendationResponse> results = new ArrayList<>();

            for (SongData song : filteredSongs)
                results.add(MoodRecommendationResponse.builder()
                        .title(song.title())
                        .artist(song.artist())
                        .artwork(song.artwork())
                        .duration(song.duration())
                        .bpm(song.bpm())
                        .audioUrl(song.audioUrl())
                        .mood(userMood)
                        .build());

            String jsonResponse = objectMapper.writeValueAsString(results);

            sendResponse(msg, jsonResponse);

        } catch (Exception e) {
            log.error("Error processing mood request", e);
            sendError(msg, e.getMessage());
        }
    }

    private void sendResponse(ACLMessage msg, String content) {
        ACLMessage reply = msg.createReply();
        reply.setContent(content);
        reply.setConversationId(msg.getConversationId());
        reply.addReceiver(new AID("BridgeAgent", AID.ISLOCALNAME));
        send(reply);
    }

    private void sendError(ACLMessage msg, String error) {
        sendResponse(msg, "ERROR: " + error);
    }
}