package com.ticketon.ai.evaluation.generation;

import com.ticketon.ai.policy.answer.service.PolicyAnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.generation-evaluation.enabled",
        havingValue = "true"
)
public class GenerationEvaluationRunner implements ApplicationRunner {

    private final GenerationEvaluationLoader evaluationLoader;
    private final GenerationEvaluator evaluator;
    private final GenerationEvaluationResultWriter resultWriter;
    private final PolicyAnswerService policyAnswerService;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        String runId = UUID.randomUUID().toString();
        logRunConfiguration(runId);

        GenerationEvaluationResult result = evaluator.evaluate(
                evaluationLoader.load()
        );
        Path resultPath = resultWriter.write(runId, result);

        result.caseResults().stream()
                .filter(caseResult -> !caseResult.automaticCheckPass()
                        || Boolean.FALSE.equals(caseResult.retrievalPass()))
                .forEach(this::logFailure);

        log.info(
                """
                Generation 평가 완료
                - runId: {}
                - Retrieval 성공: {}/{}
                - 자동 구조 검사 성공: {}/{}
                - Structured Output 성공: {}/{}
                - Abstention 성공: {}/{}
                - 평균 Generation latency: {}ms
                - 결과 파일: {}
                """,
                runId,
                result.retrievalPassCount(),
                result.retrievalApplicableCount(),
                result.automaticCheckPassCount(),
                result.totalCount(),
                result.structuredOutputPassCount(),
                result.totalCount(),
                result.abstentionPassCount(),
                result.totalCount(),
                String.format("%.0f", result.averageGenerationLatencyMs()),
                resultPath
        );
    }

    private void logRunConfiguration(String runId) {
        log.info(
                "Generation 평가 시작: runId={}, dataset={}, generatorModel={}, promptVersion={}, thinking={}, numCtx={}, retrievalVersion={}",
                runId,
                environment.getProperty("app.generation-evaluation.dataset", "development"),
                environment.getProperty("spring.ai.ollama.chat.options.model", "qwen3:8b"),
                policyAnswerService.promptVersion(),
                false,
                environment.getProperty("spring.ai.ollama.chat.options.num-ctx", "ollama-default"),
                "retrieval-v2"
        );
    }

    private void logFailure(GenerationEvaluationResult.CaseResult caseResult) {
        log.warn(
                "[FAIL] {} | expected={} | retrieved={} | retrievalPass={} | automaticCheckPass={} | structuredOutputReason={} | answer={}",
                caseResult.id(),
                caseResult.expectedPolicyIds(),
                caseResult.retrievedPolicyIds(),
                caseResult.retrievalPass(),
                caseResult.automaticCheckPass(),
                caseResult.structuredOutputFailureReason(),
                caseResult.answer()
        );
    }
}
