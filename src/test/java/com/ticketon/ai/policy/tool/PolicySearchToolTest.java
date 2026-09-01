package com.ticketon.ai.policy.tool;

import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import com.ticketon.ai.policy.search.service.PolicyRetrievalService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicySearchToolTest {

    @Test
    void 기존_Retrieval_파이프라인의_Top3를_그대로_반환한다() {
        PolicyRetrievalService retrievalService = mock(PolicyRetrievalService.class);
        PolicySearchTool tool = new PolicySearchTool(retrievalService);
        List<PolicySearchResponse> policies = List.of(
                new PolicySearchResponse(
                        "SEAT-02",
                        "QUEUE_SEAT",
                        "좌석 임시 선점 시간",
                        "좌석은 7분간 임시 선점됩니다.",
                        0.9
                )
        );
        when(retrievalService.retrieve("좌석은 몇 분 동안 잡혀 있어요?"))
                .thenReturn(policies);

        List<PolicySearchResponse> result =
                tool.searchPolicies("좌석은 몇 분 동안 잡혀 있어요?");

        assertThat(result).isEqualTo(policies);
    }
}
