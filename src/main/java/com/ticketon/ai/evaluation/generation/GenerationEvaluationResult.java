package com.ticketon.ai.evaluation.generation;

import java.util.List;

public record GenerationEvaluationResult(
        int totalCount,
        int retrievalApplicableCount,
        int retrievalPassCount,
        int automaticCheckPassCount,
        int structuredOutputPassCount,
        int abstentionPassCount,
        double averageGenerationLatencyMs,
        List<CaseResult> caseResults
) {

    public record CaseResult(
            String id,
            String category,
            String question,
            List<String> expectedPolicyIds,
            List<String> retrievedPolicyIds,
            Boolean retrievalPass,
            List<String> mustIncludeFacts,
            List<String> mustNotIncludeFacts,
            boolean shouldAbstain,
            boolean automaticCheckPass,
            boolean abstentionPass,
            String answer,
            List<String> usedPolicyIds,
            boolean abstained,
            boolean structuredOutputPass,
            String structuredOutputFailureReason,
            long generationLatencyMs
    ) {
    }
}
