package dev.project.atlas.api;

import dev.project.atlas.service.AgentConnClient;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
public class ChatController {
    private final AgentConnClient agentConnClient;

    public ChatController(AgentConnClient agentConnClient) {
        this.agentConnClient = agentConnClient;
    }

    @MessageMapping("/execute")
    public void executePrompt(Map<String, String> payload) {
        String message = payload.get("message");
        if (message != null && !message.trim().isEmpty()) {
            agentConnClient.streamPrompt(message);
        }
    }
}