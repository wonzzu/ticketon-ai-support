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
        name = "app.tool-selection-evaluation.enabled",
        havingValue = "true"
)
public class ToolSelectionEvaluationRunner implements ApplicationRunner {

    private final ToolSelectionEvaluationLoader evaluationLoader;
    private final ToolSelectionEvaluator evaluator;

    @Override
    public void run(ApplicationArguments args) {
        ToolSelectionEvaluationResult result =
                evaluator.evaluate(evaluationLoader.load());

        result.caseResults().stream()
                .filter(caseResult -> !caseResult.success())
                .forEach(this::logFailure);

        log.info(
                """
                지원 경로 평가 완료
                - 정답: {}/{}
                - 모델 오류: {}
                - Route Accuracy: {}
                """,
                result.successCount(),
                result.totalCount(),
                result.modelErrorCount(),
                String.format("%.3f", result.accuracy())
        );
    }

    private void logFailure(ToolSelectionEvaluationResult.CaseResult caseResult) {
        log.warn(
                "[FAIL] {} | category={} | authenticated={} | expected={} | actual={} | errorType={} | question={}",
                caseResult.id(),
                caseResult.category(),
                caseResult.authenticated(),
                caseResult.expectedRoute(),
                caseResult.actualRoute(),
                caseResult.errorType(),
                caseResult.question()
        );
    }
}
