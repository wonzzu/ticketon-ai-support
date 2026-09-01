package com.ticketon.ai.policy.context.service;

import com.ticketon.ai.policy.context.dto.PolicyContext;
import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyContextService {

    public PolicyContext build(List<PolicySearchResponse> policies) {
        String content = policies.stream()
                .map(this::formatPolicy)
                .reduce((first, second) -> first + "\n\n" + second)
                .orElse("");

        return PolicyContext.of(content, policies);
    }

    private String formatPolicy(PolicySearchResponse policy) {
        return """
                [POLICY_START]
                policyId: %s
                title: %s
                content:
                %s
                [POLICY_END]
                """.formatted(
                policy.policyId(),
                policy.title(),
                policy.content()
        ).strip();
    }
}
