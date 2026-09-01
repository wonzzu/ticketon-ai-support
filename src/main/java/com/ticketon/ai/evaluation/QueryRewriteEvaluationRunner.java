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
        name = "app.query-rewrite-evaluation.enabled",
        havingValue = "true"
)
public class QueryRewriteEvaluationRunner implements ApplicationRunner {

    private final PolicySearchEvaluationLoader evaluationLoader;
    private final QueryRewriteEvaluator evaluator;

    @Override
    public void run(ApplicationArguments args) {
        QueryRewriteEvaluationResult result =
                evaluator.evaluate(evaluationLoader.load());

        result.caseResults().stream()
                .filter(caseResult ->
                        caseResult.outcome()
                                != QueryRewriteEvaluationResult.Outcome.MAINTAINED
                )
                .forEach(this::logCaseResult);

        log.info(
                """
                Query Rewrite 평가 완료
                - Baseline 성공: {}/{}
                - Rewrite 성공: {}/{}
                - Baseline Recall@3: {}
                - Rewrite Recall@3: {}
                - 개선: {}
                - 유지: {}
                - 악화: {}
                - 미개선: {}
                """,
                result.baselineSuccessCount(),
                result.totalCount(),
                result.rewrittenSuccessCount(),
                result.totalCount(),
                String.format("%.2f", result.baselineRecallAt3()),
                String.format("%.2f", result.rewrittenRecallAt3()),
                result.improvedCount(),
                result.maintainedCount(),
                result.regressedCount(),
                result.unchangedFailureCount()
        );
    }

    private void logCaseResult(
            QueryRewriteEvaluationResult.CaseResult caseResult
    ) {
        log.info(
                "[{}] {} | expected={} | baseline={} | rewritten={} | rewrite={}",
                caseResult.outcome(),
                caseResult.id(),
                caseResult.expectedPolicyIds(),
                caseResult.baselineRetrievedPolicyIds(),
                caseResult.rewrittenRetrievedPolicyIds(),
                caseResult.rewrittenQuestion()
        );
    }
}
