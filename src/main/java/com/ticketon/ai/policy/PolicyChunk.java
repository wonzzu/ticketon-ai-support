package com.ticketon.ai.policy;

import java.time.LocalDate;

public record PolicyChunk(
        String chunkId,
        String policyId,
        String documentType,
        String audience,
        String implementationStatus,
        String domain,
        String title,
        String content,
        String version,
        LocalDate effectiveFrom,
        String status
) {
}
