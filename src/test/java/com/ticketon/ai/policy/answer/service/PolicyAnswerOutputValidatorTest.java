package com.ticketon.ai.policy.answer.service;

import com.ticketon.ai.policy.answer.domain.PolicyAnswerGeneration;
import com.ticketon.ai.policy.answer.domain.PolicyAnswerModelOutput;
import com.ticketon.ai.policy.context.dto.PolicyContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyAnswerOutputValidatorTest {

    private final PolicyAnswerOutputValidator validator =
            new PolicyAnswerOutputValidator();

    @Test
    void rejectsPolicyIdThatWasNotRetrieved() {
        PolicyAnswerModelOutput output = new PolicyAnswerModelOutput(
                "정책에 따른 답변입니다.",
                List.of("UNKNOWN-01"),
                false
        );

        PolicyAnswerGeneration generation = validator.validate(
                output,
                context("QUEUE-04")
        );

        assertThat(generation.structuredOutputPass()).isFalse();
        assertThat(generation.answer())
                .isEqualTo("제공된 정책만으로 확인할 수 없습니다.");
        assertThat(generation.usedPolicyIds()).isEmpty();
    }

    @Test
    void keepsOnlyRetrievedPoliciesUsedByTheAnswer() {
        PolicyAnswerModelOutput output = new PolicyAnswerModelOutput(
                "대기열 입장 권한은 10분입니다.",
                List.of("QUEUE-04", "QUEUE-04"),
                false
        );

        PolicyAnswerGeneration generation = validator.validate(
                output,
                context("QUEUE-04", "SEAT-02")
        );

        assertThat(generation.structuredOutputPass()).isTrue();
        assertThat(generation.usedPolicyIds()).containsExactly("QUEUE-04");
        assertThat(generation.abstained()).isFalse();
    }

    @Test
    void abstainedAnswerMustNotHavePolicySources() {
        PolicyAnswerModelOutput output = new PolicyAnswerModelOutput(
                "제공된 정책만으로 확인할 수 없습니다.",
                List.of("QUEUE-04"),
                true
        );

        PolicyAnswerGeneration generation = validator.validate(
                output,
                context("QUEUE-04")
        );

        assertThat(generation.structuredOutputPass()).isFalse();
        assertThat(generation.usedPolicyIds()).isEmpty();
    }

    private PolicyContext context(String... policyIds) {
        List<PolicyContext.Source> sources = List.of(policyIds).stream()
                .map(policyId -> new PolicyContext.Source(policyId, policyId))
                .toList();

        return new PolicyContext("context", sources);
    }
}
