package dev.project.atlas.model;

public record UserFact(
        String factId,
        String category,
        String topic,
        String content,
        String timestamp
) {}
