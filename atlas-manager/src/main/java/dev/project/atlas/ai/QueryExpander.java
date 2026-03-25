package dev.project.atlas.ai;

import dev.project.atlas.model.ContextPayload;
import dev.project.atlas.service.MemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryExpander {

    private final MemoryService memoryService;

    // Notice we return the object now, not a String
    public ContextPayload buildExpandedPayload(String agentSelection, String rawPrompt) {

        String userFacts = memoryService.getFacts();
        String vaultData = memoryService.searchVault(rawPrompt);

        return new ContextPayload(agentSelection, rawPrompt, userFacts, vaultData);
    }
}