package com.ticketon.ai.evaluation;

import com.ticketon.ai.support.domain.SupportRoute;

import java.util.List;

public record ToolSelectionEvaluationResult(
        int totalCount,
        int successCount,
        int modelErrorCount,
        double accuracy,
        List<CaseResult> caseResults
) {

    public record CaseResult(
            String id,
            String category,
            boolean authenticated,
            String question,
            SupportRoute expectedRoute,
            SupportRoute actualRoute,
            boolean success,
            String errorType
    ) {
    }
}
