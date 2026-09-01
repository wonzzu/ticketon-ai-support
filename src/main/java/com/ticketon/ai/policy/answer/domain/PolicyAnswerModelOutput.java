package com.ticketon.ai.policy.answer.domain;

import java.util.List;

public record PolicyAnswerModelOutput(
        String answer,
        List<String> usedPolicyIds,
        boolean abstained
) {
}
