package dev.project.atlas.api;

import dev.project.atlas.ai.InputGuardrail;
import dev.project.atlas.ai.QueryExpander;
import dev.project.atlas.ai.TaskDecomposer;
import dev.project.atlas.model.*;
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
        // 1. use guardrail
        Double score = inputGuardrail.evaluateResponse(chatRequest.prompt());
        System.out.println("Guardrail score: " + score);

        if (score < 0.5) {
            messagingTemplate.convertAndSend("/topic/chat-stream",
                    "Atlas: Request blocked. Score (" + score + ") is too low for web execution.");
            return;
        }

        // 2. query expansion
        ContextPayload payload = queryExpander.buildExpandedPayload(chatRequest.agentSelection(), chatRequest.prompt());
        System.out.println(" ==== Expanded payload ==== \n" + payload);
        messagingTemplate.convertAndSend("/topic/chat-stream", " ==== Expanded payload ==== \n" + payload);

        // 3. decompose into tasks
        TaskPlan plan = taskDecomposer.decomposeToPlan(payload.toString());
        System.out.println(" ==== Task plan ==== \n" + plan.toString());
        messagingTemplate.convertAndSend("/topic/chat-stream", " ==== Task plan ==== \n" + plan.toString());

        // 4. initialize workflow state
        WorkFlow activeWorkFlow = new WorkFlow(chatRequest.agentSelection(), plan);
        System.out.println("Initialized WorkFlow ID: " + activeWorkFlow.getWorkflowId());

        // 5. send to python...
    }
}
