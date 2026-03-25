package dev.project.atlas.model;

import lombok.Data;

@Data
public class WorkFlowStep {
    private int stepIndex;
    private String status; // PENDING, IN_PROGRESS, SUCCESS, FAILED
    private String result; // result or error
    private ActionType action;

    public WorkFlowStep(int stepIndex, TaskStep action) {
        this.stepIndex = stepIndex;
        this.action = action.action();
        this.status = "PENDING";
        this.result = null;
    }
}
