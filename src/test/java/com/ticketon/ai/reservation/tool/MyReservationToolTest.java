package com.ticketon.ai.reservation.tool;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.client.TicketOnClient;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.tool.result.ToolFailureCode;
import com.ticketon.ai.tool.result.ToolResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MyReservationToolTest {

    private final TicketOnClient ticketOnClient = mock(TicketOnClient.class);
    private final MyReservationTool tool = new MyReservationTool(ticketOnClient);

    @Test
    void ToolContext의_JWT로_본인_예매만_조회한다() {
        TicketOnAccessToken token = new TicketOnAccessToken("user-token");
        ToolContext context = new ToolContext(Map.of(
                MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY,
                token
        ));
        List<MyReservationSummary> reservations = List.of();
        when(ticketOnClient.getMyReservations(token)).thenReturn(reservations);

        assertThat(tool.getMyReservations(context))
                .isEqualTo(ToolResult.success(reservations));
        verify(ticketOnClient).getMyReservations(token);
    }

    @Test
    void JWT가_없으면_TicketOn을_호출하지_않는다() {
        ToolContext context = new ToolContext(Map.of());

        assertThat(tool.getMyReservations(context))
                .isEqualTo(ToolResult.failure(ToolFailureCode.AUTH_REQUIRED));
        verify(ticketOnClient, never()).getMyReservations(any());
    }
}
