package com.ticketon.ai.evaluation.generation;

import com.ticketon.ai.policy.answer.domain.PolicyAnswerGeneration;
import com.ticketon.ai.policy.answer.service.PolicyAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerationAnswerBatchService {

    private final PolicyAnswerService policyAnswerService;

    public List<GeneratedAnswerCase> generate(
            List<GenerationEvaluationCase> evaluationCases
    ) {
        List<GeneratedAnswerCase> generatedAnswers = new ArrayList<>();

        for (GenerationEvaluationCase evaluationCase : evaluationCases) {
            generatedAnswers.add(generateAnswer(evaluationCase));
        }

        return List.copyOf(generatedAnswers);
    }

    private GeneratedAnswerCase generateAnswer(
            GenerationEvaluationCase evaluationCase
    ) {
        long startedAt = System.nanoTime();
        PolicyAnswerGeneration generation = policyAnswerService.generate(
                evaluationCase.question()
        );
        long latencyMs = (System.nanoTime() - startedAt) / 1_000_000;

        return new GeneratedAnswerCase(evaluationCase, generation, latencyMs);
    }
}
