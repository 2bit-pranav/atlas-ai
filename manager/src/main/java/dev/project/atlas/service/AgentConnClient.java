package dev.project.atlas.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class AgentConnClient {
    private final WebClient webClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public AgentConnClient(WebClient.Builder webClientBuilder, SimpMessagingTemplate messagingTemplate) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8000").build();
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void streamPrompt(String userPrompt) {
        Map<String, Object> payload = Map.of(
                "input", Map.of("message", userPrompt)
        );

        Flux<String> stream = this.webClient.post()
                .uri("/atlas/stream")
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(String.class);

        stream.subscribe(
                rawChunk -> {
                    try {
                        JsonNode rootNode = objectMapper.readTree(rawChunk);
                        JsonNode msgNode = rootNode.path("message");

                        if (msgNode.isArray() && !msgNode.isEmpty()) {
                            JsonNode lastMsg = msgNode.get(msgNode.size() - 1);
                            String msgType = lastMsg.path("type").toString();

                            if ("ai".equals(msgType)) {
                                String content = lastMsg.path("content").toString();
                                messagingTemplate.convertAndSend("/topic/chat", content);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Skipping malformed chunk");
                    }
                },
                error -> {
                    System.err.println("Error in stream: " + error.getMessage());
                    messagingTemplate.convertAndSend("/topic/error", "[ERROR] Connection lost");
                },
                () -> {
                    System.out.println("Streaming complete");
                    messagingTemplate.convertAndSend("/topic/chat", "[DONE]");
                }
        );
    }
}