package dev.project.atlas.model;

import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class WorkFlow{
    private String workflowId;
    private String agentMode;
    private List<WorkFlowStep> steps;

    public WorkFlow(String agentMode, TaskPlan plan) {
        this.workflowId = UUID.randomUUID().toString();
        this.agentMode = agentMode;
        this.steps = IntStream.range(0, plan.steps().size())
                .mapToObj(i -> new WorkFlowStep(i, plan.steps().get(i)))
                .collect(Collectors.toList());
    }
}
