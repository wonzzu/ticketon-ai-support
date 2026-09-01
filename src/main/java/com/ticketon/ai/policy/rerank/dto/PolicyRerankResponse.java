package com.ticketon.ai.policy.rerank.dto;

import com.ticketon.ai.policy.search.dto.PolicySearchResponse;

import java.util.List;

public record PolicyRerankResponse(
        List<PolicySearchResponse> policies,
        boolean outputIssue
) {
}
