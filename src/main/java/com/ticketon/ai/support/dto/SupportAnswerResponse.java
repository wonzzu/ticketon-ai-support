package com.ticketon.ai.support.dto;

import com.ticketon.ai.policy.answer.dto.PolicyAnswerResponse;

import java.util.List;

public record SupportAnswerResponse(
        String answer,
        List<Source> sources,
        String notice
) {

    public static SupportAnswerResponse from(String answer) {
        return new SupportAnswerResponse(
                answer,
                List.of(),
                ""
        );
    }

    public static SupportAnswerResponse from(
            PolicyAnswerResponse policyAnswer,
            String notice
    ) {
        List<Source> sources = policyAnswer.sources().stream()
                .map(Source::from)
                .toList();

        return new SupportAnswerResponse(
                policyAnswer.answer(),
                sources,
                notice
        );
    }

    public record Source(
            String policyId,
            String title,
            String content
    ) {

        private static Source from(PolicyAnswerResponse.Source source) {
            return new Source(
                    source.policyId(),
                    source.title(),
                    source.content()
            );
        }
    }
}
