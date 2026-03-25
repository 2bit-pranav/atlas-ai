package dev.project.atlas;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=========================================");
        System.out.println("🧪 NUCLEAR ISOLATION TEST: OLLAMA");
        System.out.println("=========================================");

        try {
            // We manually build the model here to bypass Spring Boot entirely
            ChatLanguageModel manualModel = OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("gemma3-4b-tools:latest")
                    .temperature(0.0)
                    .build();

            System.out.println("Model built successfully. Pinging Ollama...");

            // Send a raw, simple prompt
            String response = manualModel.generate("Reply with exactly one word: 'Acknowledged'.");

            System.out.println("✅ SUCCESS! Ollama replied: " + response);

        } catch (Exception e) {
            System.err.println("❌ RAW OLLAMA CONNECTION FAILED!");
            e.printStackTrace();
        }
        System.out.println("=========================================");
    }
}