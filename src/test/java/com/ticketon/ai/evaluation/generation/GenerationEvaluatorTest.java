package com.ticketon.ai.evaluation.generation;

import com.ticketon.ai.policy.answer.domain.PolicyAnswerGeneration;
import com.ticketon.ai.policy.context.dto.PolicyContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GenerationEvaluatorTest {

    @Test
    void 답변_불가_문항은_Retrieval을_제외하고_Generation으로_E2E를_판단한다() {
        GenerationAnswerBatchService batchService = mock(GenerationAnswerBatchService.class);
        GenerationEvaluator evaluator = new GenerationEvaluator(batchService);

        GenerationEvaluationCase answerable = evaluationCase(
                "GEN-001",
                List.of("POLICY-01"),
                false
        );
        GenerationEvaluationCase unanswerable = evaluationCase(
                "GEN-002",
                List.of(),
                true
        );
        GeneratedAnswerCase answered = generatedAnswer(
                answerable,
                "정책에 근거한 답변입니다.",
                List.of(new PolicyContext.Source("POLICY-01", "정책"))
        );
        GeneratedAnswerCase abstained = generatedAnswer(
                unanswerable,
                "제공된 정책만으로 확인할 수 없습니다.",
                List.of(new PolicyContext.Source("POLICY-99", "유사 정책"))
        );

        when(batchService.generate(List.of(answerable, unanswerable)))
                .thenReturn(List.of(answered, abstained));
        GenerationEvaluationResult result = evaluator.evaluate(
                List.of(answerable, unanswerable)
        );

        assertThat(result.retrievalApplicableCount()).isEqualTo(1);
        assertThat(result.retrievalPassCount()).isEqualTo(1);
        assertThat(result.automaticCheckPassCount()).isEqualTo(2);
        assertThat(result.caseResults().get(1).retrievalPass()).isNull();
    }

    private GenerationEvaluationCase evaluationCase(
            String id,
            List<String> expectedPolicyIds,
            boolean shouldAbstain
    ) {
        return new GenerationEvaluationCase(
                id,
                "CATEGORY",
                "질문",
                expectedPolicyIds,
                List.of(),
                List.of(),
                shouldAbstain
        );
    }

    private GeneratedAnswerCase generatedAnswer(
            GenerationEvaluationCase evaluationCase,
            String answer,
            List<PolicyContext.Source> sources
    ) {
        PolicyAnswerGeneration generation = new PolicyAnswerGeneration(
                answer,
                new PolicyContext("정책 Context", sources),
                sources.stream().map(PolicyContext.Source::policyId).toList(),
                answer.equals("제공된 정책만으로 확인할 수 없습니다."),
                true,
                ""
        );

        return new GeneratedAnswerCase(evaluationCase, generation, 100);
    }
}
