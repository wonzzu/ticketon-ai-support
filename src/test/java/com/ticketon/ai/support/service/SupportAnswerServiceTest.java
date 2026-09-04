package com.ticketon.ai.support.service;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.observation.AiStageObservation;
import com.ticketon.ai.policy.answer.domain.PolicyAnswerGeneration;
import com.ticketon.ai.policy.answer.dto.PolicyAnswerResponse;
import com.ticketon.ai.policy.answer.service.PolicyAnswerService;
import com.ticketon.ai.policy.context.dto.PolicyContext;
import com.ticketon.ai.refund.tool.RefundEstimateTool;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.reservation.service.ReservationSelectionService;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import com.ticketon.ai.support.domain.SupportRoute;
import com.ticketon.ai.tool.result.ToolFailureCode;
import com.ticketon.ai.tool.result.ToolResult;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDateTime;
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
    private final ReservationSelectionService reservationSelectionService =
            mock(ReservationSelectionService.class);
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
                reservationSelectionService,
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

    @Test
    void 여러_예매_중_선택된_예매만_환불액을_계산한다() {
        String question = "내 최근 예매를 취소하면 환불액이 얼마야?";
        TicketOnAccessToken accessToken = new TicketOnAccessToken("access-token");
        MyReservationSummary first = reservation(11L, "첫 번째 공연");
        MyReservationSummary selected = reservation(22L, "최근 공연");
        List<MyReservationSummary> reservations = List.of(first, selected);

        when(routeService.route(question))
                .thenReturn(SupportRoute.REFUND_CALCULATION);
        when(myReservationTool.getMyReservations(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ToolResult.success(reservations));
        when(reservationSelectionService.select(question, reservations))
                .thenReturn(Optional.of(selected));
        when(refundEstimateTool.estimateRefund(
                org.mockito.ArgumentMatchers.eq(22L),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(ToolResult.failure(ToolFailureCode.NOT_FOUND));

        String answer = service.answer(question, Optional.of(accessToken));

        assertThat(answer).isEqualTo(ToolFailureCode.NOT_FOUND.getSafeMessage());
        verify(refundEstimateTool).estimateRefund(
                org.mockito.ArgumentMatchers.eq(22L),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void 결제_대기_예매는_환불액을_계산하지_않는다() {
        String question = "내 최근 예매를 취소하면 얼마 받아?";
        MyReservationSummary pending = reservation(
                11L,
                "결제 대기 공연",
                "PENDING"
        );

        when(routeService.route(question))
                .thenReturn(SupportRoute.REFUND_CALCULATION);
        when(myReservationTool.getMyReservations(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ToolResult.success(List.of(pending)));

        String answer = service.answer(
                question,
                Optional.of(new TicketOnAccessToken("access-token"))
        );

        assertThat(answer).contains("결제 대기");
        verify(reservationSelectionService, never()).select(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList()
        );
        verify(refundEstimateTool, never()).estimateRefund(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void 결제_완료_예매가_있어도_결제_대기_예매를_물으면_환불액을_계산하지_않는다() {
        String question = "내 결제 대기 예매는 환불액이 얼마야?";
        MyReservationSummary pending = reservation(11L, "결제 대기 공연", "PENDING");
        MyReservationSummary confirmed = reservation(22L, "결제 완료 공연", "CONFIRMED");

        when(routeService.route(question))
                .thenReturn(SupportRoute.REFUND_CALCULATION);
        when(myReservationTool.getMyReservations(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ToolResult.success(List.of(pending, confirmed)));

        String answer = service.answer(
                question,
                Optional.of(new TicketOnAccessToken("access-token"))
        );

        assertThat(answer).contains("결제 대기");
        verify(reservationSelectionService, never()).select(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList()
        );
        verify(refundEstimateTool, never()).estimateRefund(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void 결제_완료_예매가_있어도_이미_취소된_예매를_물으면_환불액을_계산하지_않는다() {
        String question = "이미 취소된 내 예매의 환불액을 다시 계산해줘.";
        MyReservationSummary canceled = reservation(11L, "취소된 공연", "CANCEL");
        MyReservationSummary confirmed = reservation(22L, "결제 완료 공연", "CONFIRMED");

        when(routeService.route(question))
                .thenReturn(SupportRoute.REFUND_CALCULATION);
        when(myReservationTool.getMyReservations(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ToolResult.success(List.of(canceled, confirmed)));

        String answer = service.answer(
                question,
                Optional.of(new TicketOnAccessToken("access-token"))
        );

        assertThat(answer).contains("이미 취소");
        verify(reservationSelectionService, never()).select(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList()
        );
        verify(refundEstimateTool, never()).estimateRefund(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void 환불액_계산에서는_결제_완료_예매만_선택_후보로_사용한다() {
        String question = "내 예매 환불액 알려줘.";
        MyReservationSummary pending = reservation(11L, "결제 대기 공연", "PENDING");
        MyReservationSummary confirmed = reservation(22L, "결제 완료 공연", "CONFIRMED");
        MyReservationSummary canceled = reservation(33L, "취소된 공연", "CANCEL");

        when(routeService.route(question))
                .thenReturn(SupportRoute.REFUND_CALCULATION);
        when(myReservationTool.getMyReservations(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ToolResult.success(List.of(pending, confirmed, canceled)));
        when(reservationSelectionService.select(question, List.of(confirmed)))
                .thenReturn(Optional.of(confirmed));
        when(refundEstimateTool.estimateRefund(
                org.mockito.ArgumentMatchers.eq(22L),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(ToolResult.failure(ToolFailureCode.NOT_FOUND));

        service.answer(
                question,
                Optional.of(new TicketOnAccessToken("access-token"))
        );

        verify(reservationSelectionService).select(question, List.of(confirmed));
        verify(refundEstimateTool).estimateRefund(
                org.mockito.ArgumentMatchers.eq(22L),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void 결제_완료_예매를_특정하지_못하면_날짜와_상태를_포함해_다시_묻는다() {
        String question = "내 표 하나 취소하면 얼마 받아?";
        MyReservationSummary first = reservation(11L, "첫 번째 공연", "CONFIRMED");
        MyReservationSummary second = reservation(22L, "두 번째 공연", "CONFIRMED");
        List<MyReservationSummary> reservations = List.of(first, second);

        when(routeService.route(question))
                .thenReturn(SupportRoute.REFUND_CALCULATION);
        when(myReservationTool.getMyReservations(org.mockito.ArgumentMatchers.any()))
                .thenReturn(ToolResult.success(reservations));
        when(reservationSelectionService.select(question, reservations))
                .thenReturn(Optional.empty());

        String answer = service.answer(
                question,
                Optional.of(new TicketOnAccessToken("access-token"))
        );

        assertThat(answer)
                .contains("첫 번째 공연", "두 번째 공연")
                .contains("2026-09-20T19:00")
                .contains("CONFIRMED")
                .contains("다시 알려주세요");
        verify(refundEstimateTool, never()).estimateRefund(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    private MyReservationSummary reservation(Long reservationId, String eventTitle) {
        return reservation(reservationId, eventTitle, "CONFIRMED");
    }

    private MyReservationSummary reservation(
            Long reservationId,
            String eventTitle,
            String reservationStatus
    ) {
        return new MyReservationSummary(
                reservationId,
                eventTitle,
                LocalDateTime.of(2026, 9, 20, 19, 0),
                reservationStatus,
                LocalDateTime.of(2026, 8, 20, 12, 0)
        );
    }
}
