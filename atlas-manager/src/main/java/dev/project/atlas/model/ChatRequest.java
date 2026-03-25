package dev.project.atlas.model;

public record ChatRequest(
        String agentSelection, // "JOB_RESEARCHER", "YT_ANALYZER", "FORM_FILLING"
        String prompt
) {}
