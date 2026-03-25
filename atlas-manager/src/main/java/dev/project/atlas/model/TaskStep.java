package dev.project.atlas.model;

import org.jetbrains.annotations.NotNull;

public record TaskStep(
    ActionType action,
    String target,
    String value,
    String description
){
    @NotNull
    @Override
    public String toString() {
        String val = (value == null) ? "" : value;
        // [CLICK] Target: '#search-hero-headline' | Value: '' -> Click on LinkedIn search bar
        return String.format("[%s] Target: '%s' | Value: '%s' -> %s", action, target, val, description);
    }
}
