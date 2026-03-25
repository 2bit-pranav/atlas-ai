package dev.project.atlas.model;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public record ContextPayload(
        String agentMode,
        String rawPrompt,
        String userFacts,
        String vaultDocuments
) {
    // 1. The Strict String sent to the LLM
    public String toLlmPrompt() {
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
               """.formatted(agentMode, rawPrompt, userFacts, vaultDocuments);
    }

    // 2. The Beautified String for your Terminal/UI
    @NotNull
    @Override
    public String toString() {
        return """
               
               ╭─────── ATLAS CONTEXT ENGINE ─────────────────────────
               │ Agent: %s
               │ Goal:  %s
               ├─────────────────────────────────────────────────────
               │ EPISODIC MEMORY (Facts):
               %s
               │
               │ SEMANTIC VAULT (Documents):
               %s
               ╰─────────────────────────────────────────────────────
               """.formatted(
                agentMode,
                rawPrompt,
                indentForTerminal(userFacts),
                indentForTerminal(vaultDocuments)
        );
    }

    // Helper to keep the ASCII borders aligned
    private String indentForTerminal(String text) {
        if (text == null || text.isBlank()) return "│  (None found)";
        return text.lines()
                .map(line -> "│  " + line)
                .collect(Collectors.joining("\n"));
    }
}