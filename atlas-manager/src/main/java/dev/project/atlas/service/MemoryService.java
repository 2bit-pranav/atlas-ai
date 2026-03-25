package dev.project.atlas.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.project.atlas.model.UserFact;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoryService {
    private final ObjectMapper objectMapper;
    private final EmbeddingModel embeddingModel;
    private InMemoryEmbeddingStore<TextSegment> vectorVault;
    private List<UserFact> episodicMemory = new ArrayList<>();

    private final String PREFS_FILE = "src/main/resources/data/preferences.json";
    private final String VAULT_FILE = "src/main/resources/data/vault/embeddings.json";

    @PostConstruct
    private void init() {
        System.out.println("Initializing Memory Service");
        loadPrefs();
        loadVault();
    }

    private void loadPrefs() {
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            try {
                episodicMemory = objectMapper.readValue(file, new TypeReference<>() {});
                System.out.println("Loaded episodic memory from " + PREFS_FILE);
            } catch (Exception e) {
                System.err.println("Failed to load preferences: " + e.getMessage());
            }
        } else {
            System.out.println("No existing preferences found. Starting with empty memory.");
        }
    }

    private void loadVault() {
        File file = new File(VAULT_FILE);
        if (file.exists()) {
            try {
                vectorVault = InMemoryEmbeddingStore.fromFile(VAULT_FILE);
                System.out.println("Loaded vector vault from " + VAULT_FILE);
            } catch (Exception e) {
                System.err.println("Failed to load vault: " + e.getMessage());
            }
        } else {
            vectorVault = new InMemoryEmbeddingStore<>();
            System.out.println("No existing vault found. Starting with empty memory.");
            seedMockVault();
        }
    }

    public String getFacts() {
        if (episodicMemory.isEmpty()) return "No preferences found.";
        return episodicMemory.stream()
                .map(fact -> "- [" + fact.topic() + "] " + fact.content())
                .collect(Collectors.joining("\n"));
    }

    public String searchVault(String query) {
        if (vectorVault == null) return "";

        // 1. convert query to vector
        var queryEmbedding = embeddingModel.embed(query).content();

        // 2. search vault for similar vectors
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(2)
                .minScore(0.6)
                .build();
        EmbeddingSearchResult<TextSegment> searchResult = vectorVault.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();
        if (matches.isEmpty()) return "No relevant documents found";

        // 3. extract text and return
        return matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));
    }

    private void seedMockVault() {
        System.out.println("   🌱 Seeding Vector Vault with resume data...");

        // 1. Create our "Document Chunks" (Simulating a parsed PDF)
        TextSegment chunk1 = TextSegment.from("Pranav Chandak is an engineering student based in Mumbai, India, pursuing a Diploma in Electronics and Computer Science.");
        TextSegment chunk2 = TextSegment.from("Pranav's notable projects include developing a real-time F1 2020 telemetry dashboard and building a computer vision system for counting boxes using YOLOv5 and ByteTracker.");
        TextSegment chunk3 = TextSegment.from("Pranav has experience building robust backend systems using Java, Spring Boot, C++, and Python.");

        // 2. Convert text to math (Embeddings) and save to RAM
        vectorVault.add(embeddingModel.embed(chunk1).content(), chunk1);
        vectorVault.add(embeddingModel.embed(chunk2).content(), chunk2);
        vectorVault.add(embeddingModel.embed(chunk3).content(), chunk3);

        // 3. Save the RAM vault to the hard drive so we never have to re-embed this data!
        vectorVault.serializeToFile(VAULT_FILE);
        System.out.println("   💾 Vault successfully embedded and saved to: " + VAULT_FILE);
    }
}
