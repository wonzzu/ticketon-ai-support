package com.ticketon.ai.policy.tool;

import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import com.ticketon.ai.policy.search.service.PolicyRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PolicySearchTool {

    private final PolicyRetrievalService policyRetrievalService;

    @Tool(description = "TicketOn의 일반 정책, 이용 방법, 제한 조건을 검색합니다. 개인의 실제 예매나 결제 상태가 아니라 정책 근거가 필요한 질문에 사용합니다.")
    public List<PolicySearchResponse> searchPolicies(
            @ToolParam(description = "사용자가 물어본 TicketOn 정책 질문") String question
    ) {
        return policyRetrievalService.retrieve(question);
    }
}
