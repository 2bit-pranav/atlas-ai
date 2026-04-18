package dev.project.atlas.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
public class OllamaConfig {

    @Value("${lc4j.ollama.base-url}")
    private String baseUrl;

    @Value("${lc4j.ollama.chat-model.tier1.model-name}")
    private String tier1ModelName;

    @Value("${lc4j.ollama.chat-model.tier2.model-name}")
    private String tier2ModelName;

    @Value("${lc4j.ollama.chat-model.temperature}")
    private Double temperature;

    @Value("${lc4j.ollama.embedding-model.model-name}")
    private String embeddingModelName;

    @Bean("tier1Model")
    public ChatLanguageModel tier1Model() {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(tier1ModelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(30)) // Fast timeout so it fails quickly if Ollama hangs
                .logRequests(true)
                .logResponses(true)
                .format("json")
                .build();
    }

    @Bean("tier2Model")
    @Primary // default model
    public ChatLanguageModel tier2Model() {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(tier2ModelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(120)) // Allow more time to load the heavy model into RAM
                .logRequests(true)
                .logResponses(true)
                .format("json")
                .build();
    }

    @Bean
    public EmbeddingModel ollamaEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(embeddingModelName)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}