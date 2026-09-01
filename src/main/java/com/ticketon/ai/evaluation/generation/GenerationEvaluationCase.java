package com.ticketon.ai.evaluation.generation;

import java.util.List;

public record GenerationEvaluationCase(
        String id,
        String category,
        String question,
        List<String> expectedPolicyIds,
        List<String> mustIncludeFacts,
        List<String> mustNotIncludeFacts,
        boolean shouldAbstain
) {}