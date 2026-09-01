package com.ticketon.ai.evaluation;

import com.ticketon.ai.policy.rewrite.service.PolicyQueryRewriteService;
import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import com.ticketon.ai.policy.search.service.PolicySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryRewriteEvaluator {

    private final PolicyQueryRewriteService queryRewriteService;
    private final PolicySearchService policySearchService;

    public QueryRewriteEvaluationResult evaluate(
            List<PolicySearchEvaluationCase> evaluationCases
    ) {
        List<QueryRewriteEvaluationResult.CaseResult> caseResults =
                evaluationCases.stream()
                        .map(this::evaluateCase)
                        .toList();

        int expectedPolicyCount = evaluationCases.stream()
                .mapToInt(evaluationCase ->
                        evaluationCase.expectedPolicyIds().size()
                )
                .sum();

        int baselineMatchedPolicyCount = caseResults.stream()
                .mapToInt(caseResult ->
                        caseResult.baselineMatchedPolicyIds().size()
                )
                .sum();

        int rewrittenMatchedPolicyCount = caseResults.stream()
                .mapToInt(caseResult ->
                        caseResult.rewrittenMatchedPolicyIds().size()
                )
                .sum();

        return new QueryRewriteEvaluationResult(
                evaluationCases.size(),
                countBaselineSuccess(caseResults),
                countRewrittenSuccess(caseResults),
                calculateRecall(baselineMatchedPolicyCount, expectedPolicyCount),
                calculateRecall(rewrittenMatchedPolicyCount, expectedPolicyCount),
                countOutcome(
                        caseResults,
                        QueryRewriteEvaluationResult.Outcome.IMPROVED
                ),
                countOutcome(
                        caseResults,
                        QueryRewriteEvaluationResult.Outcome.MAINTAINED
                ),
                countOutcome(
                        caseResults,
                        QueryRewriteEvaluationResult.Outcome.REGRESSED
                ),
                countOutcome(
                        caseResults,
                        QueryRewriteEvaluationResult.Outcome.UNCHANGED_FAILURE
                ),
                caseResults
        );
    }

    private QueryRewriteEvaluationResult.CaseResult evaluateCase(
            PolicySearchEvaluationCase evaluationCase
    ) {
        List<String> baselineRetrievedPolicyIds =
                searchPolicyIds(evaluationCase.question());

        String rewrittenQuestion =
                queryRewriteService.rewrite(evaluationCase.question());

        List<String> rewrittenRetrievedPolicyIds =
                searchPolicyIds(rewrittenQuestion);

        List<String> baselineMatchedPolicyIds = findMatchedPolicyIds(
                evaluationCase.expectedPolicyIds(),
                baselineRetrievedPolicyIds
        );

        List<String> rewrittenMatchedPolicyIds = findMatchedPolicyIds(
                evaluationCase.expectedPolicyIds(),
                rewrittenRetrievedPolicyIds
        );

        boolean baselineSuccess =
                baselineMatchedPolicyIds.size()
                        == evaluationCase.expectedPolicyIds().size();

        boolean rewrittenSuccess =
                rewrittenMatchedPolicyIds.size()
                        == evaluationCase.expectedPolicyIds().size();

        return new QueryRewriteEvaluationResult.CaseResult(
                evaluationCase.id(),
                evaluationCase.category(),
                evaluationCase.question(),
                rewrittenQuestion,
                evaluationCase.expectedPolicyIds(),
                baselineRetrievedPolicyIds,
                rewrittenRetrievedPolicyIds,
                baselineMatchedPolicyIds,
                rewrittenMatchedPolicyIds,
                baselineSuccess,
                rewrittenSuccess,
                determineOutcome(baselineSuccess, rewrittenSuccess)
        );
    }

    private List<String> searchPolicyIds(String question) {
        return policySearchService.search(question).stream()
                .map(PolicySearchResponse::policyId)
                .toList();
    }

    private List<String> findMatchedPolicyIds(
            List<String> expectedPolicyIds,
            List<String> retrievedPolicyIds
    ) {
        return expectedPolicyIds.stream()
                .filter(retrievedPolicyIds::contains)
                .toList();
    }

    private QueryRewriteEvaluationResult.Outcome determineOutcome(
            boolean baselineSuccess,
            boolean rewrittenSuccess
    ) {
        if (!baselineSuccess && rewrittenSuccess) {
            return QueryRewriteEvaluationResult.Outcome.IMPROVED;
        }

        if (baselineSuccess && rewrittenSuccess) {
            return QueryRewriteEvaluationResult.Outcome.MAINTAINED;
        }

        if (baselineSuccess) {
            return QueryRewriteEvaluationResult.Outcome.REGRESSED;
        }

        return QueryRewriteEvaluationResult.Outcome.UNCHANGED_FAILURE;
    }

    private int countBaselineSuccess(
            List<QueryRewriteEvaluationResult.CaseResult> caseResults
    ) {
        return (int) caseResults.stream()
                .filter(QueryRewriteEvaluationResult.CaseResult::baselineSuccess)
                .count();
    }

    private int countRewrittenSuccess(
            List<QueryRewriteEvaluationResult.CaseResult> caseResults
    ) {
        return (int) caseResults.stream()
                .filter(QueryRewriteEvaluationResult.CaseResult::rewrittenSuccess)
                .count();
    }

    private int countOutcome(
            List<QueryRewriteEvaluationResult.CaseResult> caseResults,
            QueryRewriteEvaluationResult.Outcome outcome
    ) {
        return (int) caseResults.stream()
                .filter(caseResult -> caseResult.outcome() == outcome)
                .count();
    }

    private double calculateRecall(
            int matchedPolicyCount,
            int expectedPolicyCount
    ) {
        if (expectedPolicyCount == 0) {
            return 0.0;
        }

        return (double) matchedPolicyCount / expectedPolicyCount;
    }
}
