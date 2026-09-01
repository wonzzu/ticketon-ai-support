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
@ConditionalOnProperty(name = "app.policy-evaluation.enabled", havingValue = "true")
public class PolicySearchEvaluationRunner implements ApplicationRunner {

    private final PolicySearchEvaluationLoader evaluationLoader;
    private final PolicySearchEvaluator evaluator;

    @Override
    public void run(ApplicationArguments args) {
        PolicySearchEvaluationResult result = evaluator.evaluate(evaluationLoader.load());

        result.caseResults().forEach(this::logCaseResult);

        log.info(
                "정책 검색 평가 완료: 성공={}/{}, Recall@3={}",
                result.successCount(),
                result.totalCount(),
                String.format("%.2f", result.recallAt3())
        );
    }

    private void logCaseResult(PolicySearchEvaluationResult.CaseResult caseResult) {
        if (caseResult.success()) {
            log.info(
                    "[PASS] {} | expected={} | retrieved={}",
                    caseResult.id(),
                    caseResult.expectedPolicyIds(),
                    caseResult.retrievedPolicyIds()
            );
            return;
        }

        log.warn(
                "[FAIL] {} | question={} | expected={} | retrieved={}",
                caseResult.id(),
                caseResult.question(),
                caseResult.expectedPolicyIds(),
                caseResult.retrievedPolicyIds()
        );
    }
}
