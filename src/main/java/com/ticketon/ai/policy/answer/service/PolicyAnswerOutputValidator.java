package com.ticketon.ai.policy.answer.service;

import com.ticketon.ai.policy.answer.domain.PolicyAnswerGeneration;
import com.ticketon.ai.policy.answer.domain.PolicyAnswerModelOutput;
import com.ticketon.ai.policy.context.dto.PolicyContext;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PolicyAnswerOutputValidator {

    private static final String ABSTENTION_ANSWER =
            "제공된 정책만으로 확인할 수 없습니다.";

    public PolicyAnswerGeneration validate(
            PolicyAnswerModelOutput output,
            PolicyContext context
    ) {
        if (output == null || output.answer() == null || output.answer().isBlank()) {
            return invalid(context, "구조화된 답변 내용이 비어 있습니다.");
        }

        List<String> usedPolicyIds = normalize(output.usedPolicyIds());
        Set<String> retrievedPolicyIds = retrievedPolicyIds(context);

        if (!retrievedPolicyIds.containsAll(usedPolicyIds)) {
            return invalid(context, "검색되지 않은 정책 ID가 사용되었습니다.");
        }

        if (output.abstained() && !usedPolicyIds.isEmpty()) {
            return invalid(context, "답변 거부 결과에 사용 정책 ID가 포함되었습니다.");
        }

        if (!output.abstained() && usedPolicyIds.isEmpty()) {
            return invalid(context, "정상 답변에 사용 정책 ID가 없습니다.");
        }

        return new PolicyAnswerGeneration(
                output.answer().strip(),
                context,
                usedPolicyIds,
                output.abstained(),
                true,
                ""
        );
    }

    public PolicyAnswerGeneration invalid(
            PolicyContext context,
            String failureReason
    ) {
        return new PolicyAnswerGeneration(
                ABSTENTION_ANSWER,
                context,
                List.of(),
                true,
                false,
                failureReason
        );
    }

    private List<String> normalize(List<String> policyIds) {
        if (policyIds == null) {
            return List.of();
        }

        return policyIds.stream()
                .filter(policyId -> policyId != null && !policyId.isBlank())
                .map(String::strip)
                .distinct()
                .toList();
    }

    private Set<String> retrievedPolicyIds(PolicyContext context) {
        Set<String> policyIds = new HashSet<>();
        context.sources().forEach(source -> policyIds.add(source.policyId()));
        return Set.copyOf(policyIds);
    }
}
