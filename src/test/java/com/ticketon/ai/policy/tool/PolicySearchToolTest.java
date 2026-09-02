package com.ticketon.ai.policy.tool;

import com.ticketon.ai.policy.context.dto.PolicyContext;
import com.ticketon.ai.policy.context.service.PolicyContextService;
import com.ticketon.ai.policy.evidence.domain.PolicyEvidenceSufficiency;
import com.ticketon.ai.policy.evidence.service.PolicyEvidenceSufficiencyService;
import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import com.ticketon.ai.policy.search.service.PolicyRetrievalService;
import com.ticketon.ai.policy.tool.dto.PolicySearchToolResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicySearchToolTest {

    private final PolicyRetrievalService retrievalService =
            mock(PolicyRetrievalService.class);

    private final PolicyContextService contextService =
            mock(PolicyContextService.class);

    private final PolicyEvidenceSufficiencyService evidenceService =
            mock(PolicyEvidenceSufficiencyService.class);

    private final PolicySearchTool tool = new PolicySearchTool(
            retrievalService,
            contextService,
            evidenceService
    );

    @Test
    void 근거가_충분하면_검색된_정책을_반환한다() {
        String question = "좌석은 몇 분 동안 잡혀 있어요?";
        List<PolicySearchResponse> policies = policies();
        PolicyContext context = mock(PolicyContext.class);

        when(retrievalService.retrieve(question)).thenReturn(policies);
        when(contextService.build(policies)).thenReturn(context);
        when(evidenceService.evaluate(question, context))
                .thenReturn(PolicyEvidenceSufficiency.sufficient(true));

        PolicySearchToolResult result = tool.searchPolicies(question);

        assertThat(result.sufficient()).isTrue();
        assertThat(result.policies()).isEqualTo(policies);
    }

    @Test
    void 근거가_부족하면_정책을_LLM에_넘기지_않는다() {
        String question = "학생 할인 있어요?";
        List<PolicySearchResponse> policies = policies();
        PolicyContext context = mock(PolicyContext.class);

        when(retrievalService.retrieve(question)).thenReturn(policies);
        when(contextService.build(policies)).thenReturn(context);
        when(evidenceService.evaluate(question, context))
                .thenReturn(PolicyEvidenceSufficiency.sufficient(false));

        PolicySearchToolResult result = tool.searchPolicies(question);

        assertThat(result.sufficient()).isFalse();
        assertThat(result.policies()).isEmpty();
    }

    private List<PolicySearchResponse> policies() {
        return List.of(
                new PolicySearchResponse(
                        "SEAT-02",
                        "QUEUE_SEAT",
                        "좌석 임시 선점 시간",
                        "좌석은 7분간 임시 선점됩니다.",
                        0.9
                )
        );
    }
}
