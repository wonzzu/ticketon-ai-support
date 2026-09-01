package com.ticketon.ai.evaluation.generation;

public record GenerationEvaluationRunReport(
        String runId,
        String executedAt,
        String dataset,
        String generatorModel,
        String generatorPromptVersion,
        String generatorPrompt,
        boolean thinking,
        String numCtx,
        String retrievalVersion,
        GenerationEvaluationResult result
) {
}
