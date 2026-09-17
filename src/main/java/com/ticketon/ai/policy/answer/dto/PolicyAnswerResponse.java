package com.ticketon.ai.policy.answer.dto;

import com.ticketon.ai.policy.answer.domain.PolicyAnswerGeneration;
import com.ticketon.ai.policy.context.dto.PolicyContext;

import java.util.List;

public record PolicyAnswerResponse(
        String answer,
        List<Source> sources,
        boolean abstained
) {

    public static PolicyAnswerResponse from(PolicyAnswerGeneration generation) {
        List<Source> sources = generation.abstained()
                ? List.of()
                : generation.context().sources().stream()
                        .map(Source::from)
                        .toList();

        return new PolicyAnswerResponse(
                generation.answer(),
                sources,
                generation.abstained()
        );
    }

    public record Source(
            String policyId,
            String title,
            String content
    ) {

        private static Source from(PolicyContext.Source source) {
            return new Source(
                    source.policyId(),
                    source.title(),
                    source.content()
            );
        }
    }
}
