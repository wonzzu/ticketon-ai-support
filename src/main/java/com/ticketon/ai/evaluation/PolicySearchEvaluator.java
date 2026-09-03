package com.ticketon.ai.evaluation;

import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import com.ticketon.ai.policy.search.service.PolicySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicySearchEvaluator {

    private final PolicySearchService policySearchService;

    public PolicySearchEvaluationResult evaluate(List<PolicySearchEvaluationCase> evaluationCases) {
        List<PolicySearchEvaluationResult.CaseResult> caseResults = evaluationCases.stream()
                .map(this::evaluateCase)
                .toList();

        int successCount = (int) caseResults.stream()
                .filter(PolicySearchEvaluationResult.CaseResult::success)
                .count();

        int expectedPolicyCount = evaluationCases.stream()
                .mapToInt(evaluationCase -> evaluationCase.expectedPolicyIds().size())
                .sum();

        int matchedPolicyCount = caseResults.stream()
                .mapToInt(caseResult -> caseResult.matchedPolicyIds().size())
                .sum();

        double recallAt3 = expectedPolicyCount == 0
                ? 0.0
                : (double) matchedPolicyCount / expectedPolicyCount;

        return new PolicySearchEvaluationResult(
                evaluationCases.size(),
                successCount,
                recallAt3,
                caseResults
        );
    }

    private PolicySearchEvaluationResult.CaseResult evaluateCase(PolicySearchEvaluationCase evaluationCase) {

        List<String> retrievedPolicyIds = policySearchService.search(evaluationCase.question()).stream()
                .map(PolicySearchResponse::policyId)
                .toList();

        List<String> matchedPolicyIds = evaluationCase.expectedPolicyIds().stream()
                .filter(retrievedPolicyIds::contains)
                .toList();

        boolean success = matchedPolicyIds.size() == evaluationCase.expectedPolicyIds().size();

        return new PolicySearchEvaluationResult.CaseResult(
                evaluationCase.id(),
                evaluationCase.category(),
                evaluationCase.question(),
                evaluationCase.expectedPolicyIds(),
                retrievedPolicyIds,
                matchedPolicyIds,
                success
        );
    }
}
