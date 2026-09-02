package com.ticketon.ai.refund.tool;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.client.TicketOnClient;
import com.ticketon.ai.client.TicketOnClientException;
import com.ticketon.ai.refund.RefundCalculator;
import com.ticketon.ai.refund.RefundEstimate;
import com.ticketon.ai.refund.RefundSnapshot;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import com.ticketon.ai.tool.result.ToolFailureCode;
import com.ticketon.ai.tool.result.ToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class RefundEstimateTool {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private final TicketOnClient ticketOnClient;
    private final RefundCalculator refundCalculator;

    @Tool(description = """
            로그인한 사용자의 특정 예매에 대한 취소 가능 여부, 취소 수수료와 예상 환불액을 계산합니다.
            먼저 getMyReservations로 대상 예매를 확인한 뒤 반환된 reservationId를 사용하세요.
            사용자에게 reservationId를 직접 물어보지 마세요.
            """)
    public ToolResult<RefundEstimate> estimateRefund(
            @ToolParam(description = "getMyReservations가 반환한 예매 식별자")
            Long reservationId,
            ToolContext toolContext
    ) {
        Object value = toolContext.getContext()
                .get(MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY);

        if (!(value instanceof TicketOnAccessToken accessToken)) {
            return ToolResult.failure(ToolFailureCode.AUTH_REQUIRED);
        }

        try {
            RefundSnapshot snapshot = ticketOnClient.getRefundSnapshot(
                    reservationId,
                    accessToken
            );
            RefundEstimate estimate = refundCalculator.calculate(
                    snapshot,
                    LocalDateTime.now(SERVICE_ZONE)
            );

            return ToolResult.success(estimate);
        } catch (TicketOnClientException exception) {
            return ToolResult.failure(exception.getFailureCode());
        }
    }
}
