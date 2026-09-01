package com.ticketon.ai.evaluation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.rerank-evaluation.enabled",
        havingValue = "true"
)
public class RerankEvaluationRunner implements ApplicationRunner {

    private final PolicySearchEvaluationLoader evaluationLoader;
    private final RerankEvaluator evaluator;

    @Override
    public void run(ApplicationArguments args) {
        RerankEvaluationResult result =
                evaluator.evaluate(evaluationLoader.load());

        result.caseResults().stream()
                .filter(caseResult ->
                        caseResult.outcome()
                                != RerankEvaluationResult.Outcome.MAINTAINED
                                || caseResult.outputIssue()
                )
                .forEach(this::logCaseResult);

        log.info(
                """
                Reranker 평가 완료
                - Baseline 성공: {}/{}
                - Reranker 성공: {}/{}
                - Baseline Recall@3: {}
                - Reranker Recall@3: {}
                - 개선: {}
                - 유지: {}
                - 악화: {}
                - 미개선: {}
                - 출력 문제: {}
                - 평균 Reranker latency: {}ms
                - p95 Reranker latency: {}ms
                """,
                result.baselineSuccessCount(),
                result.totalCount(),
                result.rerankedSuccessCount(),
                result.totalCount(),
                String.format("%.2f", result.baselineRecallAt3()),
                String.format("%.2f", result.rerankedRecallAt3()),
                result.improvedCount(),
                result.maintainedCount(),
                result.regressedCount(),
                result.unchangedFailureCount(),
                result.outputIssueCount(),
                String.format("%.0f", result.averageRerankLatencyMs()),
                result.p95RerankLatencyMs()
        );
    }

    private void logCaseResult(RerankEvaluationResult.CaseResult caseResult) {
        log.info(
                "[{}] {} | expected={} | baseline={} | reranked={} | outputIssue={} | latency={}ms",
                caseResult.outcome(),
                caseResult.id(),
                caseResult.expectedPolicyIds(),
                caseResult.baselinePolicyIds(),
                caseResult.rerankedPolicyIds(),
                caseResult.outputIssue(),
                caseResult.rerankLatencyMs()
        );
    }
}
