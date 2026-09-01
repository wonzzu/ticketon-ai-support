package com.ticketon.ai.policy.search.dto;

import org.springframework.ai.document.Document;

import java.util.Map;

public record PolicySearchResponse(
        String policyId,
        String domain,
        String title,
        String content,
        Double similarityScore
) {

    public static PolicySearchResponse from(Document document) {
        Map<String, Object> metadata = document.getMetadata();

        return new PolicySearchResponse(
                metadata.get("policyId").toString(),
                metadata.get("domain").toString(),
                metadata.get("title").toString(),
                document.getText(),
                document.getScore()
        );
    }
}
