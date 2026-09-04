package com.ticketon.ai.support.service;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.observation.AiStageObservation;
import com.ticketon.ai.policy.answer.domain.PolicyAnswerGeneration;
import com.ticketon.ai.policy.answer.dto.PolicyAnswerResponse;
import com.ticketon.ai.policy.answer.service.PolicyAnswerService;
import com.ticketon.ai.policy.context.dto.PolicyContext;
import com.ticketon.ai.refund.tool.RefundEstimateTool;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import com.ticketon.ai.support.domain.SupportRoute;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupportAnswerServiceTest {

    private final SupportRouteService routeService =
            mock(SupportRouteService.class);
    private final PolicyAnswerService policyAnswerService =
            mock(PolicyAnswerService.class);
    private final MyReservationTool myReservationTool =
            mock(MyReservationTool.class);
    private final RefundEstimateTool refundEstimateTool =
            mock(RefundEstimateTool.class);
    private final ChatClient.Builder builder = mock(ChatClient.Builder.class);
    private final AiStageObservation observation =
            new AiStageObservation(ObservationRegistry.NOOP);

    private SupportAnswerService service;

    @BeforeEach
    void setUp() {
        service = new SupportAnswerService(
                routeService,
                policyAnswerService,
                myReservationTool,
                refundEstimateTool,
                builder,
                observation
        );
    }

    @Test
    void 정책_경로는_기존_RAG를_호출한다() {
        String question = "좌석은 몇 분 유지돼요?";
        PolicyAnswerResponse response = new PolicyAnswerResponse(
                "좌석은 7분 동안 유지됩니다.",
                List.of(),
                false
        );
        when(routeService.route(question)).thenReturn(SupportRoute.POLICY);
        when(policyAnswerService.answer(question)).thenReturn(response);

        String answer = service.answer(question, Optional.empty());

        assertThat(answer).isEqualTo(response.answer());
        verify(policyAnswerService).answer(question);
    }

    @Test
    void 개인_데이터에_JWT가_없으면_Tool을_호출하지_않는다() {
        String question = "내 예매 보여줘.";
        when(routeService.route(question)).thenReturn(SupportRoute.PERSONAL_DATA);

        String answer = service.answer(question, Optional.empty());

        assertThat(answer).contains("로그인");
        verify(myReservationTool, never()).getMyReservations(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 변경_요청은_Tool을_호출하지_않는다() {
        String question = "내 예매를 취소해줘.";
        when(routeService.route(question))
                .thenReturn(SupportRoute.UNSUPPORTED_WRITE);

        String answer = service.answer(
                question,
                Optional.of(new TicketOnAccessToken("token"))
        );

        assertThat(answer).contains("수행할 수 없습니다");
        verify(myReservationTool, never()).getMyReservations(org.mockito.ArgumentMatchers.any());
        verify(refundEstimateTool, never()).estimateRefund(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
