package dev.project.atlas.api;

import dev.project.atlas.ai.InputGuardrail;
import dev.project.atlas.ai.QueryExpander;
import dev.project.atlas.ai.TaskDecomposer;
import dev.project.atlas.model.ChatRequest;
import dev.project.atlas.model.TaskPlan;
import dev.project.atlas.model.TaskStep;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final InputGuardrail inputGuardrail;
    private final QueryExpander queryExpander;
    private final TaskDecomposer taskDecomposer;

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
        System.out.println(finalPayload);
        System.out.println(" ==== ================ ==== ");

        messagingTemplate.convertAndSend("/topic/chat-stream", finalPayload);

        // task decomposition
        TaskPlan plan = taskDecomposer.decomposeToPlan(expandedPayload);
        List<TaskStep> tasks = plan.steps();
        String finalTaskList = "Decomposed into " + tasks.size() + " steps:\n\n" + tasks.stream()
                .map(TaskStep::toString)
                .reduce("", (a, b) -> a + b + "\n");
        System.out.println("\n ==== Decomposed Tasks ==== ");
        System.out.println(finalTaskList);
        System.out.println(" ==== ================== ==== ");

        messagingTemplate.convertAndSend("/topic/chat-stream", finalTaskList);
    }
}
