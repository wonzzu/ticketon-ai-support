package com.ticketon.ai.policy.answer.domain;

import com.ticketon.ai.policy.answer.dto.PolicyAnswerResponse;
import com.ticketon.ai.policy.context.dto.PolicyContext;

import java.util.List;

public record PolicyAnswerGeneration(
        String answer,
        PolicyContext context,
        List<String> usedPolicyIds,
        boolean abstained,
        boolean structuredOutputPass,
        String structuredOutputFailureReason
) {

    private static final String ABSTENTION_ANSWER =
            "제공된 정책만으로 확인할 수 없습니다.";

    public static PolicyAnswerGeneration abstained(PolicyContext context) {
        return new PolicyAnswerGeneration(
                ABSTENTION_ANSWER,
                context,
                List.of(),
                true,
                true,
                ""
        );
    }

    public PolicyAnswerResponse toResponse() {
        return PolicyAnswerResponse.from(this);
    }
}
