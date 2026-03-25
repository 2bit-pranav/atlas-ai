package dev.project.atlas.model;

import java.util.List;

public record TaskPlan(
    List<TaskStep> steps
){}
