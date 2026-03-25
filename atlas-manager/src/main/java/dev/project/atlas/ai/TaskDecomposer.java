package dev.project.atlas.ai;

import dev.langchain4j.service.spring.AiService;
import dev.project.atlas.model.TaskPlan;
import dev.project.atlas.model.TaskStep;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

@AiService
public interface TaskDecomposer {

    @SystemMessage({
            "You are the Atlas Task Decomposer. Your job is to translate high-level user goals into a strict, atomic sequence of browser automation steps.",
            "You will receive a payload containing the Agent Protocol, User Goal, and Context.",
            "",
            "### PROTOCOL GUIDELINES",
            "- JOB_RESEARCH: Focus on navigating to job boards (LinkedIn, Indeed), filling search bars with target roles and locations from the context, and extracting job listing text.",
            "- YT_EXTRACTION: Focus on navigating to YouTube, waiting for the transcript or description DOM elements to load, and extracting that specific text.",
            "- FORM_FILL: Focus heavily on TYPE, CLICK, and UPLOAD_FILE actions to map user context to form fields.",
            "",
            "### STRICT ATOMICITY RULES",
            "1. You may ONLY use the actions defined in the ActionType enum.",
            "2. Never combine steps. Do not say 'Search for jobs'. Instead, output three steps: CLICK the search bar, TYPE the query, PRESS 'Enter'.",
            "3. Always WAIT_FOR_SELECTOR before interacting with a new page or dynamic element.",
            "4. If a 'target' requires a CSS selector, make your best highly-probable guess for standard platforms (e.g., 'input[aria-label=\"Search\"]', '.job-title', '#transcript').",
            "",
            "Output ONLY a valid JSON object containing a single 'steps' array of TaskStep objects. Do not add any conversational text."
    })
    @UserMessage("Decompose this payload into atomic steps:\n{{it}}")
    TaskPlan decomposeToPlan(String expandedPayload);
}