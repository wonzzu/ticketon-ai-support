package com.ticketon.ai.policy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class PolicyMarkdownParser {

    private static final Pattern CHUNK_HEADING = Pattern.compile(
            "^## ([A-Z][A-Z0-9]*(?:-[A-Z0-9]+)+) (.+)$"
    );
    private static final List<String> SECTION_METADATA_KEYS = List.of(
            "sourcePolicyId",
            "documentType",
            "audience",
            "implementationStatus",
            "domain"
    );

    public List<PolicyChunk> parse(Path path) {
        try {
            return parseLines(Files.readAllLines(path));
        } catch (IOException e) {
            throw new IllegalStateException("정책 Markdown 파일을 읽을 수 없습니다: " + path, e);
        }
    }

    private List<PolicyChunk> parseLines(List<String> lines) {
        List<PolicyChunk> chunks = new ArrayList<>();
        Map<String, String> metadata = parseMetadata(lines);
        String chunkId = null;
        String title = null;
        StringBuilder content = new StringBuilder();
        Map<String, String> sectionMetadata = new HashMap<>();

        for (String line : lines) {
            Matcher matcher = CHUNK_HEADING.matcher(line);
            if (matcher.matches()) {
                if (chunkId != null) {
                    chunks.add(createChunk(chunkId, title, content, metadata, sectionMetadata));
                }
                chunkId = matcher.group(1);
                title = matcher.group(2);
                content = new StringBuilder();
                sectionMetadata = new HashMap<>();
            } else if (chunkId != null && isSectionMetadata(line)) {
                String[] entry = line.split(":", 2);
                sectionMetadata.put(entry[0].trim(), removeQuotes(entry[1].trim()));
            } else if (chunkId != null) {
                content.append(line).append(System.lineSeparator());
            }
        }

        if (chunkId != null) {
            chunks.add(createChunk(chunkId, title, content, metadata, sectionMetadata));
        }

        return chunks;
    }

    private PolicyChunk createChunk(
            String chunkId,
            String title,
            StringBuilder content,
            Map<String, String> metadata,
            Map<String, String> sectionMetadata
    ) {
        return new PolicyChunk(
                chunkId,
                sectionMetadata.getOrDefault("sourcePolicyId", chunkId),
                sectionMetadata.getOrDefault(
                        "documentType",
                        metadata.getOrDefault("documentType", "POLICY")
                ),
                sectionMetadata.getOrDefault(
                        "audience",
                        metadata.getOrDefault("audience", "CUSTOMER")
                ),
                sectionMetadata.getOrDefault(
                        "implementationStatus",
                        metadata.getOrDefault("implementationStatus", "IMPLEMENTED")
                ),
                sectionMetadata.getOrDefault("domain", metadata.get("domain")),
                title,
                content.toString().trim(),
                metadata.get("version"),
                LocalDate.parse(metadata.get("effectiveFrom")),
                metadata.get("status")
        );
    }

    private boolean isSectionMetadata(String line) {
        return SECTION_METADATA_KEYS.stream()
                .anyMatch(key -> line.startsWith(key + ":"));
    }

    private Map<String, String> parseMetadata(List<String> lines) {
        Map<String, String> metadata = new HashMap<>();
        boolean insideFrontMatter = false;

        for (String line : lines) {
            if (line.equals("---")) {
                if (insideFrontMatter) {
                    break;
                }
                insideFrontMatter = true;
                continue;
            }

            if (insideFrontMatter && line.contains(":")) {
                String[] entry = line.split(":", 2);
                metadata.put(entry[0].trim(), removeQuotes(entry[1].trim()));
            }
        }

        return metadata;
    }

    private String removeQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
