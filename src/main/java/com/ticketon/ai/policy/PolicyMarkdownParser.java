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

    private static final Pattern POLICY_HEADING = Pattern.compile("^## ([A-Z]+-\\d{2}) (.+)$");

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
        String policyId = null;
        String title = null;
        StringBuilder content = new StringBuilder();

        for (String line : lines) {
            Matcher matcher = POLICY_HEADING.matcher(line);
            if (matcher.matches()) {
                if (policyId != null) {
                    chunks.add(createChunk(policyId, title, content, metadata));
                }
                policyId = matcher.group(1);
                title = matcher.group(2);
                content = new StringBuilder();
            } else if (policyId != null) {
                content.append(line).append(System.lineSeparator());
            }
        }

        if (policyId != null) {
            chunks.add(createChunk(policyId, title, content, metadata));
        }

        return chunks;
    }

    private PolicyChunk createChunk(
            String policyId,
            String title,
            StringBuilder content,
            Map<String, String> metadata
    ) {
        return new PolicyChunk(
                policyId,
                metadata.get("domain"),
                title,
                content.toString().trim(),
                metadata.get("version"),
                LocalDate.parse(metadata.get("effectiveFrom")),
                metadata.get("status")
        );
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
