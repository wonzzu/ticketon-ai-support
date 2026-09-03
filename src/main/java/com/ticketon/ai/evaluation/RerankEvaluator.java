package com.ticketon.ai.evaluation;

import com.ticketon.ai.policy.rerank.dto.PolicyRerankResponse;
import com.ticketon.ai.policy.rerank.service.PolicyRerankService;
import com.ticketon.ai.policy.rewrite.service.PolicyQueryRewriteService;
import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import com.ticketon.ai.policy.search.service.PolicySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RerankEvaluator {

    private static final int FINAL_POLICY_COUNT = 3;
    private static final double NANOSECONDS_PER_MILLISECOND = 1_000_000.0;

    private final PolicyQueryRewriteService queryRewriteService;
    private final PolicySearchService policySearchService;
    private final PolicyRerankService policyRerankService;

    public RerankEvaluationResult evaluate(
            List<PolicySearchEvaluationCase> evaluationCases
    ) {
        List<RerankEvaluationResult.CaseResult> caseResults =
                evaluationCases.stream()
                        .map(this::evaluateCase)
                        .toList();

        int expectedPolicyCount = evaluationCases.stream()
                .mapToInt(evaluationCase ->
                        evaluationCase.expectedPolicyIds().size()
                )
                .sum();

        int baselineMatchedCount = caseResults.stream()
                .mapToInt(caseResult ->
                        caseResult.baselineMatchedPolicyIds().size()
                )
                .sum();

        int rerankedMatchedCount = caseResults.stream()
                .mapToInt(caseResult ->
                        caseResult.rerankedMatchedPolicyIds().size()
                )
                .sum();

        List<Long> latencies = caseResults.stream()
                .map(RerankEvaluationResult.CaseResult::rerankLatencyMs)
                .sorted()
                .toList();

        return new RerankEvaluationResult(
                evaluationCases.size(),
                countBaselineSuccess(caseResults),
                countRerankedSuccess(caseResults),
                calculateRecall(baselineMatchedCount, expectedPolicyCount),
                calculateRecall(rerankedMatchedCount, expectedPolicyCount),
                countOutcome(caseResults, RerankEvaluationResult.Outcome.IMPROVED),
                countOutcome(caseResults, RerankEvaluationResult.Outcome.MAINTAINED),
                countOutcome(caseResults, RerankEvaluationResult.Outcome.REGRESSED),
                countOutcome(
                        caseResults,
                        RerankEvaluationResult.Outcome.UNCHANGED_FAILURE
                ),
                (int) caseResults.stream()
                        .filter(RerankEvaluationResult.CaseResult::outputIssue)
                        .count(),
                latencies.stream()
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0.0),
                calculateP95(latencies),
                caseResults
        );
    }

    private RerankEvaluationResult.CaseResult evaluateCase(
            PolicySearchEvaluationCase evaluationCase
    ) {
        String rewrittenQuestion =
                queryRewriteService.rewrite(evaluationCase.question());

        List<PolicySearchResponse> candidates =
                policySearchService.searchCandidates(rewrittenQuestion);

        List<String> baselinePolicyIds = candidates.stream()
                .limit(FINAL_POLICY_COUNT)
                .map(PolicySearchResponse::policyId)
                .toList();

        List<Double> baselineSimilarityScores = candidates.stream()
                .map(PolicySearchResponse::similarityScore)
                .toList();

        long startNanos = System.nanoTime();
        PolicyRerankResponse rerankResponse =
                policyRerankService.rerank(rewrittenQuestion, candidates);
        long rerankLatencyMs = Math.round(
                (System.nanoTime() - startNanos) / NANOSECONDS_PER_MILLISECOND
        );

        List<String> rerankedPolicyIds = rerankResponse.policies().stream()
                .limit(FINAL_POLICY_COUNT)
                .map(PolicySearchResponse::policyId)
                .toList();

        List<String> baselineMatchedPolicyIds = findMatchedPolicyIds(
                evaluationCase.expectedPolicyIds(),
                baselinePolicyIds
        );

        List<String> rerankedMatchedPolicyIds = findMatchedPolicyIds(
                evaluationCase.expectedPolicyIds(),
                rerankedPolicyIds
        );

        boolean baselineSuccess = baselineMatchedPolicyIds.size()
                == evaluationCase.expectedPolicyIds().size();
        boolean rerankedSuccess = rerankedMatchedPolicyIds.size()
                == evaluationCase.expectedPolicyIds().size();

        return new RerankEvaluationResult.CaseResult(
                evaluationCase.id(),
                rewrittenQuestion,
                evaluationCase.expectedPolicyIds(),
                baselinePolicyIds,
                baselineSimilarityScores,
                rerankedPolicyIds,
                baselineMatchedPolicyIds,
                rerankedMatchedPolicyIds,
                baselineSuccess,
                rerankedSuccess,
                rerankResponse.outputIssue(),
                rerankLatencyMs,
                determineOutcome(baselineSuccess, rerankedSuccess)
        );
    }

    private List<String> findMatchedPolicyIds(
            List<String> expectedPolicyIds,
            List<String> retrievedPolicyIds
    ) {
        return expectedPolicyIds.stream()
                .filter(retrievedPolicyIds::contains)
                .toList();
    }

    private RerankEvaluationResult.Outcome determineOutcome(
            boolean baselineSuccess,
            boolean rerankedSuccess
    ) {
        if (!baselineSuccess && rerankedSuccess) {
            return RerankEvaluationResult.Outcome.IMPROVED;
        }
        if (baselineSuccess && rerankedSuccess) {
            return RerankEvaluationResult.Outcome.MAINTAINED;
        }
        if (baselineSuccess) {
            return RerankEvaluationResult.Outcome.REGRESSED;
        }
        return RerankEvaluationResult.Outcome.UNCHANGED_FAILURE;
    }

    private int countBaselineSuccess(
            List<RerankEvaluationResult.CaseResult> caseResults
    ) {
        return (int) caseResults.stream()
                .filter(RerankEvaluationResult.CaseResult::baselineSuccess)
                .count();
    }

    private int countRerankedSuccess(
            List<RerankEvaluationResult.CaseResult> caseResults
    ) {
        return (int) caseResults.stream()
                .filter(RerankEvaluationResult.CaseResult::rerankedSuccess)
                .count();
    }

    private int countOutcome(
            List<RerankEvaluationResult.CaseResult> caseResults,
            RerankEvaluationResult.Outcome outcome
    ) {
        return (int) caseResults.stream()
                .filter(caseResult -> caseResult.outcome() == outcome)
                .count();
    }

    private double calculateRecall(int matchedCount, int expectedCount) {
        return expectedCount == 0 ? 0.0 : (double) matchedCount / expectedCount;
    }

    private long calculateP95(List<Long> sortedLatencies) {
        if (sortedLatencies.isEmpty()) {
            return 0L;
        }

        int index = (int) Math.ceil(sortedLatencies.size() * 0.95) - 1;
        return sortedLatencies.get(Math.max(index, 0));
    }
}
