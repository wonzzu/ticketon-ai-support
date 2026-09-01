package com.ticketon.ai.policy.context.dto;

import com.ticketon.ai.policy.search.dto.PolicySearchResponse;

import java.util.List;

public record PolicyContext(
        String content,
        List<Source> sources
) {

    public static PolicyContext of(String content, List<PolicySearchResponse> policies) {

        List<Source> sources = policies.stream()
                .map(Source::from)
                .toList();

        return new PolicyContext(content, sources);
    }

    public record Source(String policyId, String title) {

        private static Source from(PolicySearchResponse policy) {
            return new Source(
                    policy.policyId(),
                    policy.title()
            );
        }
    }
}