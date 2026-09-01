package com.ticketon.ai.policy.answer.service;

import com.ticketon.ai.policy.context.dto.PolicyContext;
import com.ticketon.ai.policy.context.service.PolicyContextService;
import com.ticketon.ai.policy.evidence.domain.PolicyEvidenceSufficiency;
import com.ticketon.ai.policy.evidence.service.PolicyEvidenceSufficiencyService;
import com.ticketon.ai.policy.search.service.PolicyRetrievalService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PolicyAnswerEvidenceGateTest {

    @Test
    void 근거가_부족하면_답변을_생성하지_않고_거부한다() {
        PolicyRetrievalService retrievalService = mock(PolicyRetrievalService.class);
        PolicyContextService contextService = mock(PolicyContextService.class);
        PolicyEvidenceSufficiencyService evidenceService =
                mock(PolicyEvidenceSufficiencyService.class);
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        PolicyAnswerModelOutputParser outputParser =
                mock(PolicyAnswerModelOutputParser.class);
        PolicyAnswerOutputValidator outputValidator =
                mock(PolicyAnswerOutputValidator.class);
        PolicyContext context = new PolicyContext("정책 Context", List.of());

        when(retrievalService.retrieve("학생 할인 있어요?"))
                .thenReturn(List.of());
        when(contextService.build(List.of())).thenReturn(context);
        when(evidenceService.evaluate("학생 할인 있어요?", context))
                .thenReturn(PolicyEvidenceSufficiency.sufficient(false));

        PolicyAnswerService service = new PolicyAnswerService(
                retrievalService,
                contextService,
                evidenceService,
                chatClientBuilder,
                outputParser,
                outputValidator
        );

        var generation = service.generate("학생 할인 있어요?");

        assertThat(generation.abstained()).isTrue();
        assertThat(generation.answer())
                .isEqualTo("제공된 정책만으로 확인할 수 없습니다.");
        verifyNoInteractions(chatClientBuilder);
    }
}
