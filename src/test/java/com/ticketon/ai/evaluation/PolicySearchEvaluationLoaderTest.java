package com.ticketon.ai.evaluation;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolicySearchEvaluationLoaderTest {

    private final PolicySearchEvaluationLoader loader =
            new PolicySearchEvaluationLoader(new ObjectMapper(), "development");

    @Test
    void 개발용_평가_JSON에서_쉰한_개의_문항을_읽는다() {
        List<PolicySearchEvaluationCase> evaluationCases = loader.load();

        assertThat(evaluationCases).hasSize(51);
    }

    @Test
    void 모든_평가_문항은_필수값과_중복되지_않은_ID를_가진다() {
        List<PolicySearchEvaluationCase> evaluationCases = loader.load();

        assertThat(evaluationCases)
                .extracting(PolicySearchEvaluationCase::id)
                .doesNotHaveDuplicates();

        assertThat(evaluationCases).allSatisfy(evaluationCase -> {
            assertThat(evaluationCase.id()).isNotBlank();
            assertThat(evaluationCase.category()).isNotBlank();
            assertThat(evaluationCase.question()).isNotBlank();
            assertThat(evaluationCase.expectedPolicyIds()).isNotEmpty();
            assertThat(evaluationCase.expectedPolicyIds()).allSatisfy(
                    policyId -> assertThat(policyId).isNotBlank()
            );
        });
    }

    @Test
    void 대기열_입장_권한_문항의_정답은_QUEUE_04다() {
        PolicySearchEvaluationCase evaluationCase = loader.load().stream()
                .filter(it -> it.id().equals("EVAL-010"))
                .findFirst()
                .orElseThrow();

        assertThat(evaluationCase.expectedPolicyIds()).containsExactly("QUEUE-04");
    }

    @Test
    void Holdout_평가_JSON에서_열아홉_개의_문항을_읽는다() {
        PolicySearchEvaluationLoader holdoutLoader =
                new PolicySearchEvaluationLoader(new ObjectMapper(), "holdout");

        List<PolicySearchEvaluationCase> evaluationCases = holdoutLoader.load();

        assertThat(evaluationCases).hasSize(19);
        assertThat(evaluationCases)
                .extracting(PolicySearchEvaluationCase::id)
                .doesNotHaveDuplicates();
    }

    @Test
    void 어려운_개발_평가_JSON에서_아흔네_개의_문항을_읽는다() {
        PolicySearchEvaluationLoader challengingLoader =
                new PolicySearchEvaluationLoader(
                        new ObjectMapper(),
                        "challenging-development"
                );

        List<PolicySearchEvaluationCase> evaluationCases = challengingLoader.load();

        assertThat(evaluationCases).hasSize(94);
        assertThat(evaluationCases)
                .extracting(PolicySearchEvaluationCase::id)
                .doesNotHaveDuplicates();
        assertThat(evaluationCases)
                .extracting(PolicySearchEvaluationCase::expectedPolicyIds)
                .allSatisfy(policyIds -> assertThat(policyIds).isNotEmpty());
    }
}
