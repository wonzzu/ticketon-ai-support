package com.ticketon.ai.policy.evidence.service;

import com.ticketon.ai.policy.evidence.domain.PolicyEvidenceSufficiency;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEvidenceOutputParserTest {

    private final PolicyEvidenceOutputParser parser =
            new PolicyEvidenceOutputParser(new ObjectMapper());

    @Test
    void 충분한_근거라는_JSON을_변환한다() {
        PolicyEvidenceSufficiency result = parser.parse(
                "{\"sufficient\": true}"
        );

        assertThat(result.sufficient()).isTrue();
        assertThat(result.structuredOutputPass()).isTrue();
    }

    @Test
    void 부족한_근거라는_JSON을_변환한다() {
        PolicyEvidenceSufficiency result = parser.parse(
                "{\"sufficient\": false}"
        );

        assertThat(result.sufficient()).isFalse();
        assertThat(result.structuredOutputPass()).isTrue();
    }

    @Test
    void 깨진_JSON은_근거_부족으로_안전하게_처리한다() {
        PolicyEvidenceSufficiency result = parser.parse("판정할 수 없습니다.");

        assertThat(result.sufficient()).isFalse();
        assertThat(result.structuredOutputPass()).isFalse();
    }
}
