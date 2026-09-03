package com.ticketon.ai.evaluation;

import com.ticketon.ai.support.domain.SupportRoute;

public record ToolSelectionEvaluationCase(
        String id,
        String category,
        boolean authenticated,
        String question,
        SupportRoute expectedRoute
) {
}
