package com.ticketon.ai.policy.tool;

import com.ticketon.ai.policy.context.dto.PolicyContext;
import com.ticketon.ai.policy.context.service.PolicyContextService;
import com.ticketon.ai.policy.evidence.domain.PolicyEvidenceSufficiency;
import com.ticketon.ai.policy.evidence.service.PolicyEvidenceSufficiencyService;
import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import com.ticketon.ai.policy.search.service.PolicyRetrievalService;
import com.ticketon.ai.policy.tool.dto.PolicySearchToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PolicySearchTool {

    private final PolicyRetrievalService policyRetrievalService;
    private final PolicyContextService policyContextService;
    private final PolicyEvidenceSufficiencyService evidenceSufficiencyService;

    @Tool
    public PolicySearchToolResult searchPolicies(String question) {
        List<PolicySearchResponse> policies =
                policyRetrievalService.retrieve(question);
        PolicyContext context = policyContextService.build(policies);
        PolicyEvidenceSufficiency evidence =
                evidenceSufficiencyService.evaluate(question, context);

        if (!evidence.sufficient()) {
            return PolicySearchToolResult.insufficient();
        }

        return PolicySearchToolResult.sufficient(policies);
    }
}
