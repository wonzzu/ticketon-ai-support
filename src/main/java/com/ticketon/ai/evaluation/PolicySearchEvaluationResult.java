package com.ticketon.ai.evaluation;

import java.util.List;

public record PolicySearchEvaluationResult(
        int totalCount,
        int successCount,
        double recallAt3,
        List<CaseResult> caseResults
) {

    public record CaseResult(
            String id,
            String category,
            String question,
            List<String> expectedPolicyIds,
            List<String> retrievedPolicyIds,
            List<String> matchedPolicyIds,
            boolean success
    ) {
    }
}