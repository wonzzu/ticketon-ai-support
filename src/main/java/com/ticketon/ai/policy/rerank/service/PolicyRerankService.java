package com.ticketon.ai.policy.rerank.service;

import com.ticketon.ai.observation.AiStageObservation;
import com.ticketon.ai.policy.rerank.dto.PolicyRerankResponse;
import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class PolicyRerankService {

    private static final int FINAL_POLICY_COUNT = 3;

    private static final String SYSTEM_PROMPT = """
            당신은 TicketOn 고객지원 정책 검색 결과를 재정렬하는 Reranker입니다.

            사용자 질문에 직접적인 답변 근거가 되는 정책부터 관련도가 높은 순서로 정렬하세요.

            규칙:
            - 질문에 답하지 않습니다.
            - 제공된 후보 정책만 평가합니다.
            - 후보에 없는 policyId를 만들거나 추측하지 않습니다.
            - policyId를 변경하지 않습니다.
            - 질문의 숫자, 시간, 상태, 부정 표현과 모든 검색 의도를 고려합니다.
            - 여러 정책이 함께 필요한 질문이면 필요한 정책들을 모두 높은 순위에 둡니다.
            - 가장 관련 있는 policyId 3개를 중복 없이 반환합니다.
            - 설명이나 Markdown 없이 지정된 JSON만 출력합니다.

            출력 형식:
            {"policyIds":["POLICY-01","POLICY-02"]}
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final AiStageObservation aiStageObservation;

    public PolicyRerankService(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            AiStageObservation aiStageObservation
    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.aiStageObservation = aiStageObservation;
    }

    public PolicyRerankResponse rerank(
            String question,
            List<PolicySearchResponse> candidates
    ) {
        return aiStageObservation.observe(
                "rerank",
                () -> rerankCandidates(question, candidates)
        );
    }

    private PolicyRerankResponse rerankCandidates(
            String question,
            List<PolicySearchResponse> candidates
    ) {
        if (candidates.size() <= 1) {
            return new PolicyRerankResponse(candidates, false);
        }

        String output = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(buildUserPrompt(question, candidates))
                .options(OllamaChatOptions.builder()
                        .disableThinking())
                .call()
                .content();

        return reorderCandidates(output, candidates);
    }

    private String buildUserPrompt(
            String question,
            List<PolicySearchResponse> candidates
    ) {
        StringBuilder prompt = new StringBuilder()
                .append("사용자 질문:\n")
                .append(question)
                .append("\n\n후보 정책:\n");

        for (int index = 0; index < candidates.size(); index++) {
            PolicySearchResponse candidate = candidates.get(index);
            prompt.append("\n[")
                    .append(index + 1)
                    .append("]\npolicyId: ")
                    .append(candidate.policyId())
                    .append("\ntitle: ")
                    .append(candidate.title())
                    .append("\ncontent: ")
                    .append(candidate.content())
                    .append('\n');
        }

        return prompt.toString();
    }

    private PolicyRerankResponse reorderCandidates(
            String output,
            List<PolicySearchResponse> candidates
    ) {
        Map<String, PolicySearchResponse> candidatesByPolicyId =
                candidatesByPolicyId(candidates);

        try {
            PolicyIdRanking ranking = objectMapper.readValue(
                    removeCodeFence(output),
                    PolicyIdRanking.class
            );

            return buildValidatedResponse(
                    ranking.policyIds(),
                    candidates,
                    candidatesByPolicyId
            );
        } catch (Exception e) {
            log.warn("Reranker 출력 파싱에 실패해 Vector 순서를 사용합니다.", e);
            return new PolicyRerankResponse(candidates, true);
        }
    }

    private Map<String, PolicySearchResponse> candidatesByPolicyId(
            List<PolicySearchResponse> candidates
    ) {
        Map<String, PolicySearchResponse> candidatesByPolicyId =
                new LinkedHashMap<>();

        candidates.forEach(candidate ->
                candidatesByPolicyId.put(candidate.policyId(), candidate)
        );

        return candidatesByPolicyId;
    }

    private PolicyRerankResponse buildValidatedResponse(
            List<String> rankedPolicyIds,
            List<PolicySearchResponse> candidates,
            Map<String, PolicySearchResponse> candidatesByPolicyId
    ) {
        if (rankedPolicyIds == null) {
            return new PolicyRerankResponse(candidates, true);
        }

        Set<String> validPolicyIds = new LinkedHashSet<>();
        boolean outputIssue = false;

        for (String policyId : rankedPolicyIds) {
            if (!candidatesByPolicyId.containsKey(policyId)
                    || !validPolicyIds.add(policyId)) {
                outputIssue = true;
            }
        }

        if (validPolicyIds.size() < Math.min(FINAL_POLICY_COUNT, candidates.size())) {
            outputIssue = true;
        }

        candidates.forEach(candidate -> validPolicyIds.add(candidate.policyId()));

        List<PolicySearchResponse> rerankedPolicies = new ArrayList<>();
        validPolicyIds.forEach(policyId ->
                rerankedPolicies.add(candidatesByPolicyId.get(policyId))
        );

        return new PolicyRerankResponse(rerankedPolicies, outputIssue);
    }

    private String removeCodeFence(String output) {
        if (output == null) {
            return "";
        }

        return output.strip()
                .replaceFirst("^```(?:json)?\\s*", "")
                .replaceFirst("\\s*```$", "");
    }

    private record PolicyIdRanking(List<String> policyIds) {
    }
}
