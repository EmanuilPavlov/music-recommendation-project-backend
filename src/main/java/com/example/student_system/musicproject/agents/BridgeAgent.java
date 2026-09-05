package com.example.student_system.musicproject.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BridgeAgent extends Agent {

    public static final String LIMIT_PARAM = "limit";

    private static BridgeAgent instance;
    private final ConcurrentHashMap<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    @Override
    protected void setup() {
        instance = this;
        log.info("BridgeAgent started");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String convId = msg.getConversationId();
                    log.info("BridgeAgent received response for conversationId: {}", convId);

                    CompletableFuture<String> future = pendingRequests.remove(convId);
                    if (future != null) {
                        log.info("Completing future with response: {}", msg.getContent());
                        future.complete(msg.getContent());
                    } else {
                        log.warn("No pending future found for conversationId: {}", convId);
                    }
                } else {
                    block();
                }
            }
        });
    }

    public static CompletableFuture<String> sendToAgent(String targetAgentName, String content, int limit) {
        if (instance == null) {
            log.error("BridgeAgent instance is null");
            CompletableFuture<String> f = new CompletableFuture<>();
            f.complete("NO_MATCH");
            return f;
        }

        String convId = "req-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
        CompletableFuture<String> f = new CompletableFuture<>();
        instance.pendingRequests.put(convId, f);
        log.info("Registered pending request for conversationId: {}, target: {}", convId, targetAgentName);

        instance.addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(new AID(targetAgentName, AID.ISLOCALNAME));
                msg.setContent(content);
                msg.setConversationId(convId);
                msg.addUserDefinedParameter(LIMIT_PARAM, String.valueOf(limit));
                myAgent.send(msg);
                log.info("BridgeAgent sent message to {} with conversationId: {}, content: {}, limit: {}",
                        targetAgentName, convId, content, limit);
            }
        });

        return f;
    }
}