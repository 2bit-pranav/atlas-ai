package dev.project.atlas.model;

import org.hibernate.annotations.NotFound;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public record TaskPlan(
    List<TaskStep> steps
){
    @NotNull
    @Override
    public String toString(){
        return "Decomposed into " + steps.size() + " steps:\n\n" +
                steps.stream()
                        .map(TaskStep::toString)
                        .collect(Collectors.joining("\n\n"));
    }
}
