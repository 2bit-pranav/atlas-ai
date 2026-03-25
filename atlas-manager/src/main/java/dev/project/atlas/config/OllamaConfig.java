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
    @Value("${lc4j.ollama.chat-model.base-url}")
    private String baseUrl;

    @Value("${lc4j.ollama.chat-model.model-name}")
    private String modelName;

    @Value("${lc4j.ollama.chat-model.temperature}")
    private Double temperature;

    @Value("${lc4j.ollama.embedding-model.model-name}")
    private String embeddingModelName;

    @Bean
    @Primary
    public ChatLanguageModel ollamaChatModel() {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                // allow more time to load model into RAM
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public EmbeddingModel ollamaEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(embeddingModelName)
                .build();
    }
}
