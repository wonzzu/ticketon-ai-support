package com.ticketon.ai.evaluation;

import java.util.List;

public record QueryRewriteEvaluationResult(
        int totalCount,
        int baselineSuccessCount,
        int rewrittenSuccessCount,
        double baselineRecallAt3,
        double rewrittenRecallAt3,
        int improvedCount,
        int maintainedCount,
        int regressedCount,
        int unchangedFailureCount,
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
            String category,
            String originalQuestion,
            String rewrittenQuestion,
            List<String> expectedPolicyIds,
            List<String> baselineRetrievedPolicyIds,
            List<String> rewrittenRetrievedPolicyIds,
            List<String> baselineMatchedPolicyIds,
            List<String> rewrittenMatchedPolicyIds,
            boolean baselineSuccess,
            boolean rewrittenSuccess,
            Outcome outcome
    ) {
    }
}
