package com.ticketon.ai.policy.search.service;

import com.ticketon.ai.policy.rewrite.service.PolicyQueryRewriteService;
import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyRetrievalService {

    private static final int FINAL_POLICY_COUNT = 3;

    private final PolicyQueryRewriteService queryRewriteService;
    private final PolicySearchService policySearchService;

    public List<PolicySearchResponse> retrieve(String question) {
        String rewrittenQuestion = queryRewriteService.rewrite(question);

        return policySearchService.searchCandidates(rewrittenQuestion).stream()
                .limit(FINAL_POLICY_COUNT)
                .toList();
    }
}
