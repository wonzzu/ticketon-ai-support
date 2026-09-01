package com.ticketon.ai.refund.tool;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.client.TicketOnClient;
import com.ticketon.ai.refund.RefundCalculator;
import com.ticketon.ai.refund.RefundEstimate;
import com.ticketon.ai.refund.RefundSnapshot;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefundEstimateToolTest {

    private final TicketOnClient ticketOnClient = mock(TicketOnClient.class);
    private final RefundCalculator refundCalculator = mock(RefundCalculator.class);
    private final RefundEstimateTool tool = new RefundEstimateTool(
            ticketOnClient,
            refundCalculator
    );

    @Test
    void JWT로_본인_예매_스냅샷을_조회해_Java_계산결과를_반환한다() {
        TicketOnAccessToken token = new TicketOnAccessToken("user-token");
        ToolContext context = new ToolContext(Map.of(
                MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY,
                token
        ));
        RefundSnapshot snapshot = new RefundSnapshot(
                10L,
                "CONFIRMED",
                LocalDateTime.of(2026, 9, 1, 10, 0),
                LocalDateTime.of(2026, 9, 15, 18, 0),
                50_000,
                "PAID"
        );
        RefundEstimate estimate = new RefundEstimate(
                10L,
                true,
                14,
                0,
                0,
                50_000,
                List.of("REFUND-02", "REFUND-04"),
                "수수료 없음"
        );
        when(ticketOnClient.getRefundSnapshot(10L, token)).thenReturn(snapshot);
        when(refundCalculator.calculate(any(), any())).thenReturn(estimate);

        RefundEstimate result = tool.estimateRefund(10L, context);

        assertThat(result).isSameAs(estimate);
        verify(ticketOnClient).getRefundSnapshot(10L, token);
        verify(refundCalculator).calculate(any(RefundSnapshot.class), any());
    }

    @Test
    void JWT가_없으면_환불정보를_조회하지_않는다() {
        ToolContext context = new ToolContext(Map.of());

        assertThatThrownBy(() -> tool.estimateRefund(10L, context))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
        verify(ticketOnClient, never()).getRefundSnapshot(any(), any());
        verify(refundCalculator, never()).calculate(any(), any());
    }
}
