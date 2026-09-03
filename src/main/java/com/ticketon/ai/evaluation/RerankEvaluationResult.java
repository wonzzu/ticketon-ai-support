package com.ticketon.ai.evaluation;

import java.util.List;

public record RerankEvaluationResult(
        int totalCount,
        int baselineSuccessCount,
        int rerankedSuccessCount,
        double baselineRecallAt3,
        double rerankedRecallAt3,
        int improvedCount,
        int maintainedCount,
        int regressedCount,
        int unchangedFailureCount,
        int outputIssueCount,
        double averageRerankLatencyMs,
        long p95RerankLatencyMs,
        List<CaseResult> caseResults
) {

    public enum Outcome {
        IMPROVED,
        MAINTAINED,
        REGRESSED,
        UNCHANGED_FAILURE
    }

    public record CaseResult(
            String id,
            String rewrittenQuestion,
            List<String> expectedPolicyIds,
            List<String> baselinePolicyIds,
            List<Double> baselineSimilarityScores,
            List<String> rerankedPolicyIds,
            List<String> baselineMatchedPolicyIds,
            List<String> rerankedMatchedPolicyIds,
            boolean baselineSuccess,
            boolean rerankedSuccess,
            boolean outputIssue,
            long rerankLatencyMs,
            Outcome outcome
    ) {
    }
}
