package com.example.student_system.musicproject.config;

import com.example.student_system.musicproject.agents.MedicalConditionAgent;
import com.example.student_system.musicproject.agents.MoodAgent;
import com.example.student_system.musicproject.services.interfaces.AudiusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Order(1)
@Configuration
public class JadeStarter implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final AudiusService audiusService;
    private AgentContainer mainContainer;

    public JadeStarter(ObjectMapper objectMapper, AudiusService audiusService) {
        this.objectMapper = objectMapper;
        this.audiusService = audiusService;
    }

    @Override
    public void run(String... args) {
        Runtime rt = Runtime.instance();
        rt.setCloseVM(true);

        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "false");

        mainContainer = rt.createMainContainer(profile);

        try {
            Object[] mapperArgs = new Object[] { objectMapper };

            MoodAgent moodAgent = new MoodAgent(audiusService);
            moodAgent.setArguments(mapperArgs);
            mainContainer.acceptNewAgent("MoodAgent", moodAgent).start();

            MedicalConditionAgent medicalConditionAgent = new MedicalConditionAgent(audiusService);
            medicalConditionAgent.setArguments(mapperArgs);
            mainContainer.acceptNewAgent("MedicalConditionAgent", medicalConditionAgent).start();
            mainContainer.createNewAgent("BridgeAgent", "com.example.student_system.musicproject.agents.BridgeAgent", null).start();
            System.out.println("All agents started");

        } catch (StaleProxyException e) {
            throw new RuntimeException("Failed to start agents", e);
        }
    }

    @Bean
    public AgentContainer agentContainer() {
        return mainContainer;
    }
}