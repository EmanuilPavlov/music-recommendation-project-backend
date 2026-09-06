package com.example.student_system.musicproject.agents;

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
            int limit = Integer.parseInt(msg.getUserDefinedParameter(BridgeAgent.LIMIT_PARAM));
            log.info("ConditionAgent received request: {}, limit: {}", condition, limit);

            List<String> symptoms = ontology.getSymptomsForCondition(condition);
            if (symptoms.isEmpty()) {
                log.error("No symptoms found for condition: {}", condition);
                sendResponse(msg, "NO_RESULTS");
                return;
            }

            List<String> effects = ontology.getEffectsForCondition(condition);
            if (effects.isEmpty()) {
                log.error("No effects found for condition: {}", condition);
                sendResponse(msg, "NO_RESULTS");
                return;
            }

            List<MedicalConditionRecommendationResponse> results = new ArrayList<>();

            for (String effect : effects) {
                if (results.size() >= limit) break;

                List<String> genres = ontology.getGenresForEffect(effect);
                if (genres.isEmpty()) continue;

                List<MusicOntology.BPMRange> ranges = ontology.getBPMRangesForEffect(effect);

                List<SongData> candidates = new ArrayList<>();
                for (String genre : genres) {
                    if (candidates.size() >= 100) break;
                    if (ranges.isEmpty()) {
                        candidates.addAll(audiusService.searchTracks(genre, null, null, 100));
                        continue;
                    }
                    for (MusicOntology.BPMRange range : ranges) {
                        if (candidates.size() >= 100) break;
                        candidates.addAll(audiusService.searchTracks(genre, range.min(), range.max() - 1, 100));
                    }
                }

                for (SongData song : candidates) {
                    if (results.size() >= limit) break;

                    if (ranges.isEmpty() || ontology.isBPMInEffectRange(song.bpm(), effect)) {
                        results.add(MedicalConditionRecommendationResponse.builder()
                                .title(song.title())
                                .artist(song.artist())
                                .artwork(song.artwork())
                                .bpm(song.bpm())
                                .duration(song.duration())
                                .condition(condition)
                                .matchedEffect(effect)
                                .relatedSymptoms(symptoms)
                                .audioUrl(song.audioUrl())
                                .build());
                    }
                }
            }

            String jsonResponse = objectMapper.writeValueAsString(results);

            sendResponse(msg, jsonResponse);

        } catch (Exception e) {
            log.error("Error processing condition request", e);
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