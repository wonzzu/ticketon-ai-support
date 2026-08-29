package com.ticketon.ai.policy;

import java.time.LocalDate;

public record PolicyChunk(
        String policyId,
        String domain,
        String title,
        String content,
        String version,
        LocalDate effectiveFrom,
        String status
) {
}
