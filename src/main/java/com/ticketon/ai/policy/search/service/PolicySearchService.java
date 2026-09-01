package com.ticketon.ai.policy.search.service;

import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PolicySearchService {

    private static final int RETRIEVAL_CANDIDATE_COUNT = 10;
    private static final int FINAL_POLICY_COUNT = 3;

    private final VectorStore vectorStore;

    public List<PolicySearchResponse> search(String query) {
        return searchCandidates(query).stream()
                .limit(FINAL_POLICY_COUNT)
                .toList();
    }

    public List<PolicySearchResponse> searchCandidates(String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(RETRIEVAL_CANDIDATE_COUNT)
                .similarityThresholdAll()
                .build();

        Set<String> seenPolicyIds = new HashSet<>();

        return vectorStore.similaritySearch(request).stream()
                .filter(document -> seenPolicyIds.add(
                        document.getMetadata().get("policyId").toString()
                ))
                .map(PolicySearchResponse::from)
                .toList();
    }
}
