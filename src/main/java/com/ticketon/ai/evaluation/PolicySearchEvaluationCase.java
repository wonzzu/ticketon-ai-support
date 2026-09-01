package com.ticketon.ai.evaluation;

import java.util.List;

public record PolicySearchEvaluationCase(
        String id,
        String category,
        String question,
        List<String> expectedPolicyIds
) {
}