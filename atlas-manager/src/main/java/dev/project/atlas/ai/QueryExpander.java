package dev.project.atlas.ai;

import dev.project.atlas.service.MemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryExpander {
    private final MemoryService memoryService;

    public String buildExpandedPayload(String agentSelection, String rawQuery) {
        System.out.println("Expanding query for agent: " + agentSelection);

        // 1. gather context
        String userFacts = memoryService.getFacts();
        String vaultData = memoryService.searchVault(rawQuery);

        // 2. construct master prompt
        return """
               [SYSTEM INSTRUCTION]
               You are Atlas, executing the %s protocol. 
               Achieve the user's goal using your available browser tools.
               
               [USER GOAL]
               %s
               
               [USER CONTEXT & PREFERENCES]
               %s
               
               [RELEVANT VAULT DOCUMENTS]
               %s
               """.formatted(agentSelection, rawQuery, userFacts, vaultData);
    }
}
