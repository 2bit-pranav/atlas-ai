package dev.project.atlas.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface InputGuardrail {
    @SystemMessage({
            "You are a security and relevance filter for a agentic web application.",
            "Evaluate the user's prompt and return a single decimal number between 0.0 and 1.0.",
            "",
            "0.0 - 0.4 means the prompt is completely inappropriate or irrelevant (e.g., 'write a poem', 'what is 2+2'), conversational nonsense, or asking for code generation.",
            "0.5 to 1.0: The prompt is a valid, actionable request to automate a web task, extract data, or fill a form.",
            "",
            "Respond ONLY with the decimal number. Do not add any text, explanations, or formatting."
    })
    @UserMessage("Prompt: {{userInput}}")
    Double evaluateResponse(String userInput);
}
