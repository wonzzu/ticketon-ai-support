package com.ticketon.ai.reservation.tool;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.client.TicketOnClient;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MyReservationTool {

    public static final String ACCESS_TOKEN_CONTEXT_KEY = "ticketOnAccessToken";

    private final TicketOnClient ticketOnClient;

    @Tool(description = "로그인한 사용자의 TicketOn 예매 목록을 조회합니다. 사용자가 자신의 실제 예매, 공연 일정 또는 예매 상태를 묻는 경우에만 사용합니다.")
    public List<MyReservationSummary> getMyReservations(ToolContext toolContext) {
        Object value = toolContext.getContext().get(ACCESS_TOKEN_CONTEXT_KEY);

        if (!(value instanceof TicketOnAccessToken accessToken)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "TicketOn 로그인이 필요합니다."
            );
        }

        return ticketOnClient.getMyReservations(accessToken);
    }
}
