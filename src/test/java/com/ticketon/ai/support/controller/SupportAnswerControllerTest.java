package com.ticketon.ai.support.controller;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.policy.answer.dto.PolicyAnswerRequest;
import com.ticketon.ai.support.dto.SupportAnswerResponse;
import com.ticketon.ai.support.service.SupportAnswerService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupportAnswerControllerTest {

    private final SupportAnswerService supportAnswerService =
            mock(SupportAnswerService.class);
    private final SupportAnswerController controller =
            new SupportAnswerController(supportAnswerService);

    @Test
    void 일반_정책_질문은_인증정보_없이_전달한다() {
        PolicyAnswerRequest request = new PolicyAnswerRequest("취소 정책 알려줘.");
        when(supportAnswerService.answer(request.question(), Optional.empty()))
                .thenReturn("정책 답변");

        SupportAnswerResponse response = controller.answer(request, null);

        verify(supportAnswerService).answer(request.question(), Optional.empty());
        assertThat(response.answer()).isEqualTo("정책 답변");
    }

    @Test
    void Bearer_JWT를_현재_사용자_인증정보로_전달한다() {
        PolicyAnswerRequest request = new PolicyAnswerRequest("내 예매 보여줘.");
        TicketOnAccessToken accessToken = new TicketOnAccessToken("user-token");
        when(supportAnswerService.answer(
                request.question(),
                Optional.of(accessToken)
        )).thenReturn("개인 예매 답변");

        SupportAnswerResponse response = controller.answer(
                request,
                "Bearer user-token"
        );

        verify(supportAnswerService).answer(
                request.question(),
                Optional.of(accessToken)
        );
        assertThat(response.answer()).isEqualTo("개인 예매 답변");
    }
}
