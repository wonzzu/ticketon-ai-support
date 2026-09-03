package com.ticketon.ai.evaluation;

import com.ticketon.ai.support.domain.SupportRoute;
import com.ticketon.ai.support.service.SupportRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolSelectionEvaluator {

    private final SupportRouteService supportRouteService;

    public ToolSelectionEvaluationResult evaluate(
            List<ToolSelectionEvaluationCase> evaluationCases
    ) {
        List<ToolSelectionEvaluationResult.CaseResult> caseResults =
                evaluationCases.stream()
                        .map(this::evaluateCase)
                        .toList();
        int successCount = (int) caseResults.stream()
                .filter(ToolSelectionEvaluationResult.CaseResult::success)
                .count();
        int modelErrorCount = (int) caseResults.stream()
                .filter(caseResult -> caseResult.actualRoute() == null)
                .count();
        double accuracy = evaluationCases.isEmpty()
                ? 0.0
                : (double) successCount / evaluationCases.size();

        return new ToolSelectionEvaluationResult(
                evaluationCases.size(),
                successCount,
                modelErrorCount,
                accuracy,
                caseResults
        );
    }

    private ToolSelectionEvaluationResult.CaseResult evaluateCase(
            ToolSelectionEvaluationCase evaluationCase
    ) {
        try {
            SupportRoute actualRoute = supportRouteService.route(
                    evaluationCase.question()
            );

            return new ToolSelectionEvaluationResult.CaseResult(
                    evaluationCase.id(),
                    evaluationCase.category(),
                    evaluationCase.authenticated(),
                    evaluationCase.question(),
                    evaluationCase.expectedRoute(),
                    actualRoute,
                    evaluationCase.expectedRoute() == actualRoute,
                    ""
            );
        } catch (RuntimeException exception) {
            return new ToolSelectionEvaluationResult.CaseResult(
                    evaluationCase.id(),
                    evaluationCase.category(),
                    evaluationCase.authenticated(),
                    evaluationCase.question(),
                    evaluationCase.expectedRoute(),
                    null,
                    false,
                    exception.getClass().getSimpleName()
            );
        }
    }
}
