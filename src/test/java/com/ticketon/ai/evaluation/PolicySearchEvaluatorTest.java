package com.ticketon.ai.evaluation;

import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import com.ticketon.ai.policy.search.service.PolicySearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicySearchEvaluatorTest {

    private PolicySearchService policySearchService;
    private PolicySearchEvaluator evaluator;

    @BeforeEach
    void setUp() {
        policySearchService = mock(PolicySearchService.class);
        evaluator = new PolicySearchEvaluator(policySearchService);
    }

    @Test
    void 정답_정책이_Top3에_있으면_성공한다() {
        PolicySearchEvaluationCase evaluationCase = new PolicySearchEvaluationCase(
                "EVAL-001",
                "PARAPHRASE",
                "좌석 잡아둔 거 결제 안 하면 언제 다시 풀려요?",
                List.of("SEAT-04")
        );

        when(policySearchService.search(evaluationCase.question()))
                .thenReturn(List.of(
                        searchResponse("SEAT-02"),
                        searchResponse("SEAT-04"),
                        searchResponse("RESERVATION-01")
                ));

        PolicySearchEvaluationResult result = evaluator.evaluate(List.of(evaluationCase));

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.recallAt3()).isEqualTo(1.0);
        assertThat(result.caseResults().getFirst().success()).isTrue();
        assertThat(result.caseResults().getFirst().matchedPolicyIds())
                .containsExactly("SEAT-04");
    }

    @Test
    void 정답_정책이_Top3에_없으면_실패한다() {
        PolicySearchEvaluationCase evaluationCase = new PolicySearchEvaluationCase(
                "EVAL-010",
                "POLICY_CONFUSION",
                "대기열 입장 권한은 좌석 선점처럼 7분 동안 유지되나요?",
                List.of("QUEUE-04")
        );

        when(policySearchService.search(evaluationCase.question()))
                .thenReturn(List.of(
                        searchResponse("SEAT-04"),
                        searchResponse("QUEUE-03"),
                        searchResponse("QUEUE-02")
                ));

        PolicySearchEvaluationResult result = evaluator.evaluate(List.of(evaluationCase));

        assertThat(result.successCount()).isZero();
        assertThat(result.recallAt3()).isZero();
        assertThat(result.caseResults().getFirst().success()).isFalse();
        assertThat(result.caseResults().getFirst().matchedPolicyIds()).isEmpty();
    }

    @Test
    void 두_문제_중_한_문제를_맞히면_RecallAt3는_0점5다() {
        PolicySearchEvaluationCase successCase = new PolicySearchEvaluationCase(
                "EVAL-001",
                "PARAPHRASE",
                "좌석은 언제 풀려요?",
                List.of("SEAT-04")
        );
        PolicySearchEvaluationCase failCase = new PolicySearchEvaluationCase(
                "EVAL-010",
                "POLICY_CONFUSION",
                "입장 권한도 7분인가요?",
                List.of("QUEUE-04")
        );

        when(policySearchService.search(successCase.question()))
                .thenReturn(List.of(searchResponse("SEAT-04")));
        when(policySearchService.search(failCase.question()))
                .thenReturn(List.of(searchResponse("SEAT-04")));

        PolicySearchEvaluationResult result =
                evaluator.evaluate(List.of(successCase, failCase));

        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.recallAt3()).isEqualTo(0.5);
    }

    private PolicySearchResponse searchResponse(String policyId) {
        return new PolicySearchResponse(
                policyId,
                "TEST",
                "테스트 정책",
                "테스트 정책 본문",
                0.8
        );
    }
}
