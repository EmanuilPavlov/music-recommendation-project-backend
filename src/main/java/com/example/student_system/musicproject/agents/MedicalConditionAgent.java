package com.example.student_system.musicproject.agents;

import com.example.student_system.musicproject.dto.BPMRange;
import com.example.student_system.musicproject.dto.SongData;
import com.example.student_system.musicproject.dto.music_response.classes.MedicalConditionRecommendationResponse;
import com.example.student_system.musicproject.ontology.MusicOntology;
import com.example.student_system.musicproject.services.interfaces.AudiusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class MedicalConditionAgent extends Agent {

    private MusicOntology ontology;
    private final AudiusService audiusService;
    private ObjectMapper objectMapper;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args == null || args.length == 0 || !(args[0] instanceof ObjectMapper)) {
            log.error("MedicalConditionAgent started without a valid ObjectMapper argument, terminating agent");
            doDelete();
            return;
        }
        this.objectMapper = (ObjectMapper) args[0];

        ontology = new MusicOntology();

        log.info("ConditionAgent is ready to process requests");

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
            String condition = msg.getContent();

            Integer limit = Integer.valueOf(msg.getUserDefinedParameter(BridgeAgent.LIMIT_PARAM));
            log.info("ConditionAgent received request: {}, limit: {}", condition, limit);

            List<String> effects = ontology.getEffectsForCondition(condition);
            if (effects.isEmpty()) {
                sendResponse(msg, "NO_MATCH");
                return;
            }

            List<String> symptoms = ontology.getSymptomsForCondition(condition);
            Map<String, List<SongData>> songsByEffect = new LinkedHashMap<>();

            for (String effect : effects) {
                List<String> genres = ontology.getGenresForEffect(effect);
                List<BPMRange> bpmRanges = ontology.getBPMRangesForEffect(effect);

                if (!genres.isEmpty() && !bpmRanges.isEmpty()) {
                    List<SongData> songs = audiusService.fetchSongs(genres, bpmRanges, limit);
                    if (!songs.isEmpty()) songsByEffect.put(effect, songs);
                }
            }

            if (songsByEffect.isEmpty()) {
                sendResponse(msg, "NO_RESULTS");
                return;
            }

            List<MedicalConditionRecommendationResponse> results = selectSongsByEffect(condition, symptoms, songsByEffect, limit);

            if (results.isEmpty()) {
                sendResponse(msg, "NO_RESULTS");
                return;
            }

            String jsonResponse = objectMapper.writeValueAsString(results);

            sendResponse(msg, jsonResponse);

        } catch (Exception e) {
            log.error("Error processing condition request", e);
            sendResponse(msg,"ERROR: " + e.getMessage());
        }
    }

    private List<MedicalConditionRecommendationResponse> selectSongsByEffect(
            String condition,
            List<String> symptoms,
            Map<String, List<SongData>> songsByEffect,
            int limit
    ) {
        List<MedicalConditionRecommendationResponse> results = new ArrayList<>();
        Map<String, Integer> indexes = new HashMap<>();
        Set<String> usedSongs = new HashSet<>();

        while (results.size() < limit) {
            boolean addedSong = false;

            for (Map.Entry<String, List<SongData>> entry : songsByEffect.entrySet()) {
                if (results.size() >= limit)
                    break;

                String effect = entry.getKey();
                List<SongData> songs = entry.getValue();

                int index = indexes.getOrDefault(effect, 0);

                while (index < songs.size()) {
                    SongData song = songs.get(index);
                    index++;
                    indexes.put(effect, index);

                    String key = song.audioUrl() != null && !song.audioUrl().isBlank()
                            ? song.audioUrl()
                            : song.artist() + "|" + song.title();

                    if (!usedSongs.add(key))
                        continue;

                    results.add(
                            MedicalConditionRecommendationResponse.builder()
                                    .title(song.title())
                                    .artist(song.artist())
                                    .artwork(song.artwork())
                                    .bpm(song.bpm())
                                    .audioUrl(song.audioUrl())
                                    .duration(song.duration())
                                    .condition(condition)
                                    .matchedEffect(effect)
                                    .relatedSymptoms(symptoms)
                                    .build()
                    );

                    addedSong = true;
                    break;
                }
            }

            if (!addedSong)
                break;
        }

        return results;
    }

    private void sendResponse(ACLMessage msg, String content) {
        ACLMessage reply = msg.createReply();
        reply.setContent(content);
        reply.setConversationId(msg.getConversationId());
        reply.addReceiver(new AID("BridgeAgent", AID.ISLOCALNAME));
        send(reply);
    }

}