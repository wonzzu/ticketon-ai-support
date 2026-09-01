package com.ticketon.ai.evaluation.generation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerationEvaluator {

    private static final String ABSTENTION_ANSWER =
            "제공된 정책만으로 확인할 수 없습니다.";

    private final GenerationAnswerBatchService answerBatchService;

    public GenerationEvaluationResult evaluate(
            List<GenerationEvaluationCase> evaluationCases
    ) {
        List<GenerationEvaluationResult.CaseResult> caseResults =
                answerBatchService.generate(evaluationCases).stream()
                        .map(this::evaluateCase)
                        .toList();

        return summarize(caseResults);
    }

    private GenerationEvaluationResult.CaseResult evaluateCase(
            GeneratedAnswerCase generatedAnswer
    ) {
        GenerationEvaluationCase evaluationCase = generatedAnswer.evaluationCase();
        List<String> retrievedPolicyIds = generatedAnswer.generation()
                .context()
                .sources()
                .stream()
                .map(source -> source.policyId())
                .toList();
        Boolean retrievalPass = evaluationCase.expectedPolicyIds().isEmpty()
                ? null
                : retrievedPolicyIds.containsAll(evaluationCase.expectedPolicyIds());
        boolean abstentionPass = isAbstentionCorrect(
                generatedAnswer.generation().answer(),
                evaluationCase.shouldAbstain()
        );
        boolean automaticCheckPass = !generatedAnswer.generation().answer().isBlank()
                && generatedAnswer.generation().structuredOutputPass()
                && abstentionPass;

        return new GenerationEvaluationResult.CaseResult(
                evaluationCase.id(),
                evaluationCase.category(),
                evaluationCase.question(),
                evaluationCase.expectedPolicyIds(),
                retrievedPolicyIds,
                retrievalPass,
                evaluationCase.mustIncludeFacts(),
                evaluationCase.mustNotIncludeFacts(),
                evaluationCase.shouldAbstain(),
                automaticCheckPass,
                abstentionPass,
                generatedAnswer.generation().answer(),
                generatedAnswer.generation().usedPolicyIds(),
                generatedAnswer.generation().abstained(),
                generatedAnswer.generation().structuredOutputPass(),
                generatedAnswer.generation().structuredOutputFailureReason(),
                generatedAnswer.latencyMs()
        );
    }

    private boolean isAbstentionCorrect(String answer, boolean shouldAbstain) {
        boolean abstained = answer.strip().equals(ABSTENTION_ANSWER);
        return shouldAbstain == abstained;
    }

    private GenerationEvaluationResult summarize(
            List<GenerationEvaluationResult.CaseResult> caseResults
    ) {
        int retrievalApplicableCount = (int) caseResults.stream()
                .filter(result -> result.retrievalPass() != null)
                .count();
        int retrievalPassCount = (int) caseResults.stream()
                .filter(result -> Boolean.TRUE.equals(result.retrievalPass()))
                .count();
        double averageLatency = caseResults.stream()
                .mapToLong(GenerationEvaluationResult.CaseResult::generationLatencyMs)
                .average()
                .orElse(0.0);

        return new GenerationEvaluationResult(
                caseResults.size(),
                retrievalApplicableCount,
                retrievalPassCount,
                count(caseResults, GenerationEvaluationResult.CaseResult::automaticCheckPass),
                count(caseResults, GenerationEvaluationResult.CaseResult::structuredOutputPass),
                count(caseResults, GenerationEvaluationResult.CaseResult::abstentionPass),
                averageLatency,
                caseResults
        );
    }

    private int count(
            List<GenerationEvaluationResult.CaseResult> caseResults,
            java.util.function.Predicate<GenerationEvaluationResult.CaseResult> predicate
    ) {
        return (int) caseResults.stream().filter(predicate).count();
    }
}
