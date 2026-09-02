package com.ticketon.ai.reservation.tool;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.client.TicketOnClient;
import com.ticketon.ai.client.TicketOnClientException;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.tool.result.ToolFailureCode;
import com.ticketon.ai.tool.result.ToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MyReservationTool {

    public static final String ACCESS_TOKEN_CONTEXT_KEY = "ticketOnAccessToken";

    private final TicketOnClient ticketOnClient;

    @Tool(description = "로그인한 사용자의 TicketOn 예매 목록을 조회합니다. 사용자가 자신의 실제 예매, 공연 일정 또는 예매 상태를 묻는 경우에만 사용합니다.")
    public ToolResult<List<MyReservationSummary>> getMyReservations(
            ToolContext toolContext
    ) {
        Object value = toolContext.getContext().get(ACCESS_TOKEN_CONTEXT_KEY);

        if (!(value instanceof TicketOnAccessToken accessToken)) {
            return ToolResult.failure(ToolFailureCode.AUTH_REQUIRED);
        }

        try {
            List<MyReservationSummary> reservations =
                    ticketOnClient.getMyReservations(accessToken);

            return ToolResult.success(reservations);
        } catch (TicketOnClientException exception) {
            return ToolResult.failure(exception.getFailureCode());
        }
    }
}
