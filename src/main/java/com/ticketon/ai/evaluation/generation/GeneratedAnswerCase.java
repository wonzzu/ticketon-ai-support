package com.ticketon.ai.evaluation.generation;

import com.ticketon.ai.policy.answer.domain.PolicyAnswerGeneration;

public record GeneratedAnswerCase(
        GenerationEvaluationCase evaluationCase,
        PolicyAnswerGeneration generation,
        long latencyMs
) {
}
