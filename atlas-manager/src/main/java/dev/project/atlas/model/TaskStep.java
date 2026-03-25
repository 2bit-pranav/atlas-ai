package dev.project.atlas.model;

public record TaskStep(
    ActionType action,
    String target,
    String value,
    String description
){}
