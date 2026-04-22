package dev.project.atlas.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AgentConnClient {

    private final WebClient webClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public AgentConnClient(WebClient.Builder webClientBuilder,
                           SimpMessagingTemplate messagingTemplate) {

        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8000")
                .build();

        this.messagingTemplate = messagingTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void streamPrompt(String userPrompt) {

        Map<String, Object> payload = Map.of(
                "input", Map.of("message", userPrompt)
        );

        AtomicInteger lastLogSize = new AtomicInteger(0);
        Set<String> sentAiMessageIds = new HashSet<>();

        Flux<ServerSentEvent<String>> stream = webClient.post()
                .uri("/atlas/stream")
                .bodyValue(payload)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {});

        stream.subscribe(

                event -> {
                    try {
                        String jsonChunk = event.data();

                        if (jsonChunk == null || jsonChunk.isBlank()) {
                            return;
                        }

                        JsonNode rootNode = objectMapper.readTree(jsonChunk);

                        handleLogs(rootNode, lastLogSize);
                        handleMessages(rootNode, sentAiMessageIds);

                    } catch (Exception e) {
                        System.err.println("[PARSE ERROR] " + e.getMessage());
                    }
                },

                error -> {
                    System.err.println("[STREAM ERROR] " + error.getMessage());
                    messagingTemplate.convertAndSend(
                            "/topic/error",
                            "[ERROR] Connection lost"
                    );
                },

                () -> {
                    System.out.println("Streaming complete");
                    messagingTemplate.convertAndSend(
                            "/topic/responses",
                            "[DONE]"
                    );
                }
        );
    }

    private void handleLogs(JsonNode rootNode, AtomicInteger lastLogSize) {

        JsonNode logsNode = rootNode.path("logs");

        if (!logsNode.isArray()) {
            return;
        }

        int currentSize = logsNode.size();

        for (int i = lastLogSize.get(); i < currentSize; i++) {
            String log = logsNode.get(i).asText("");
            messagingTemplate.convertAndSend("/topic/terminal", log);
        }

        lastLogSize.set(currentSize);
    }

    private void handleMessages(JsonNode rootNode,
                                Set<String> sentAiMessageIds) {

        JsonNode msgNode = rootNode.path("messages");

        if (!msgNode.isArray()) {
            return;
        }

        for (JsonNode messageNode : msgNode) {

            String msgType = messageNode.path("type").asText("");

            if (!"ai".equals(msgType)) {
                continue;
            }

            String messageId = messageNode.path("id").asText("");

            if (messageId.isBlank()
                    || sentAiMessageIds.contains(messageId)) {
                continue;
            }

            String content =
                    extractAiText(messageNode.path("content"));

            if (!content.isBlank()) {
                messagingTemplate.convertAndSend(
                        "/topic/responses",
                        content
                );

                sentAiMessageIds.add(messageId);
            }
        }
    }

    private String extractAiText(JsonNode contentNode) {

        if (contentNode.isTextual()) {
            return contentNode.asText("");
        }

        if (contentNode.isArray()) {

            StringBuilder builder = new StringBuilder();

            for (JsonNode item : contentNode) {

                if (item.isTextual()) {
                    builder.append(item.asText(""));
                    continue;
                }

                String nested =
                        item.path("text").asText("");

                if (!nested.isBlank()) {
                    builder.append(nested);
                }
            }

            return builder.toString();
        }

        return "";
    }
}