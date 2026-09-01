package com.ticketon.ai.reservation.tool;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.client.TicketOnClient;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        assertThat(tool.getMyReservations(context)).isSameAs(reservations);
        verify(ticketOnClient).getMyReservations(token);
    }

    @Test
    void JWT가_없으면_TicketOn을_호출하지_않는다() {
        ToolContext context = new ToolContext(Map.of());

        assertThatThrownBy(() -> tool.getMyReservations(context))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
        verify(ticketOnClient, never()).getMyReservations(any());
    }
}
