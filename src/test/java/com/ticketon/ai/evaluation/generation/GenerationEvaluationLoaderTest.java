package com.ticketon.ai.evaluation.generation;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenerationEvaluationLoaderTest {

    @Test
    void Development_평가셋에서_40개_문항을_읽는다() {
        GenerationEvaluationLoader loader = createLoader("development");

        List<GenerationEvaluationCase> evaluationCases = loader.load();

        assertThat(evaluationCases).hasSize(40);
        assertThat(evaluationCases)
                .extracting(GenerationEvaluationCase::id)
                .doesNotHaveDuplicates();
    }

    @Test
    void Development_평가셋은_정해진_유형별_문항_수를_가진다() {
        GenerationEvaluationLoader loader = createLoader("development");

        List<GenerationEvaluationCase> evaluationCases = loader.load();

        assertThat(countCategory(evaluationCases, "SINGLE_POLICY")).isEqualTo(8);
        assertThat(countCategory(evaluationCases, "MULTI_POLICY")).isEqualTo(10);
        assertThat(countCategory(evaluationCases, "BOUNDARY")).isEqualTo(8);
        assertThat(countCategory(evaluationCases, "UNANSWERABLE")).isEqualTo(8);
        assertThat(countCategory(evaluationCases, "POLICY_CONFUSION")).isEqualTo(6);
    }

    @Test
    void Holdout_평가셋에서_15개_문항을_읽는다() {
        GenerationEvaluationLoader loader = createLoader("holdout");

        List<GenerationEvaluationCase> evaluationCases = loader.load();

        assertThat(evaluationCases).hasSize(15);
        assertThat(evaluationCases)
                .extracting(GenerationEvaluationCase::id)
                .doesNotHaveDuplicates();
    }

    @Test
    void 모든_문항은_채점에_필요한_값을_가진다() {
        GenerationEvaluationLoader loader = createLoader("development");

        assertThat(loader.load()).allSatisfy(evaluationCase -> {
            assertThat(evaluationCase.id()).isNotBlank();
            assertThat(evaluationCase.category()).isNotBlank();
            assertThat(evaluationCase.question()).isNotBlank();
            assertThat(evaluationCase.expectedPolicyIds()).isNotNull();
            assertThat(evaluationCase.mustIncludeFacts()).isNotNull();
            assertThat(evaluationCase.mustNotIncludeFacts()).isNotNull();
        });
    }

    private long countCategory(
            List<GenerationEvaluationCase> evaluationCases,
            String category
    ) {
        return evaluationCases.stream()
                .filter(evaluationCase -> evaluationCase.category().equals(category))
                .count();
    }

    private GenerationEvaluationLoader createLoader(String dataset) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("app.generation-evaluation.dataset", dataset);

        return new GenerationEvaluationLoader(new ObjectMapper(), environment);
    }
}
