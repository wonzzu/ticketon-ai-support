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

    @Tool(description = """
            TicketOn의 일반 정책, 이용 방법, 제한 조건과 오류 원인을 검색합니다.
            사용자의 실제 개인 예매 데이터가 필요하지 않은 TicketOn 관련 질문에 사용합니다.
            """)
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
