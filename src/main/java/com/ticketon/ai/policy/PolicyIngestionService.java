package com.ticketon.ai.policy;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.policy-ingestion.enabled", havingValue = "true")
public class PolicyIngestionService {

    private final PolicyMarkdownParser parser;
    private final VectorStore vectorStore;

    public PolicyIngestionService(PolicyMarkdownParser parser, VectorStore vectorStore) {
        this.parser = parser;
        this.vectorStore = vectorStore;
    }

    public int ingest(Path policyDirectory) {
        List<Document> documents = readPolicyChunks(policyDirectory).stream()
                .map(this::toDocument)
                .toList();

        vectorStore.add(documents);
        return documents.size();
    }

    private List<PolicyChunk> readPolicyChunks(Path policyDirectory) {
        try (var paths = Files.list(policyDirectory)) {
            return paths
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .sorted()
                    .flatMap(path -> parser.parse(path).stream())
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("정책 디렉터리를 읽을 수 없습니다: " + policyDirectory, e);
        }
    }

    private Document toDocument(PolicyChunk chunk) {
        String documentId = UUID.nameUUIDFromBytes(
                ("ticketon-policy:" + chunk.policyId()).getBytes(StandardCharsets.UTF_8)
        ).toString();

        Map<String, Object> metadata = Map.of(
                "policyId", chunk.policyId(),
                "domain", chunk.domain(),
                "title", chunk.title(),
                "version", chunk.version(),
                "effectiveFrom", chunk.effectiveFrom().toString(),
                "status", chunk.status()
        );

        return Document.builder()
                .id(documentId)
                .text(chunk.title() + System.lineSeparator() + System.lineSeparator() + chunk.content())
                .metadata(metadata)
                .build();
    }
}
