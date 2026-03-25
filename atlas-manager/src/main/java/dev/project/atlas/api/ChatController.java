package dev.project.atlas.api;

import dev.project.atlas.ai.InputGuardrail;
import dev.project.atlas.ai.QueryExpander;
import dev.project.atlas.model.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final InputGuardrail inputGuardrail;
    private final QueryExpander queryExpander;

    @MessageMapping("/chat.send")
    public void handleChatMessage(ChatRequest chatRequest) {
        // System.out.println("Incoming message: " + message);
        // return "Atlas echo: " + message;
        Double score = inputGuardrail.evaluateResponse(chatRequest.prompt());
        System.out.println("Guardrail score: " + score);

        // evaluate the request
        if (score < 0.5) {
            messagingTemplate.convertAndSend("/topic/chat-stream",
                    "Atlas: Request blocked. Score (" + score + ") is too low for web execution.");
            return;
        }

        // display the score
        // String responseMessage = "Score " + score + " accepted. Initializing " + chatRequest.agentSelection() + "...";
        // messagingTemplate.convertAndSend("/topic/chat-stream", "Atlas: " + responseMessage);

        // query expansion
        String expandedPayload = queryExpander.buildExpandedPayload(chatRequest.agentSelection(), chatRequest.prompt());
        // print expanded payload
        String finalPayload = "Score: " + score + " accepted. Initializing " + chatRequest.agentSelection() + "...\n\n" + expandedPayload;
        System.out.println("\n ==== Expanded Payload ==== ");
        System.out.println(expandedPayload);
        System.out.println(" ==== ================ ==== ");

        messagingTemplate.convertAndSend("/topic/chat-stream", finalPayload);
    }
}
