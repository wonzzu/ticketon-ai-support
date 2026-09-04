package com.ticketon.ai.support.service;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.observation.AiStageObservation;
import com.ticketon.ai.policy.answer.service.PolicyAnswerService;
import com.ticketon.ai.refund.RefundEstimate;
import com.ticketon.ai.refund.tool.RefundEstimateTool;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import com.ticketon.ai.support.domain.SupportRoute;
import com.ticketon.ai.tool.result.ToolFailureCode;
import com.ticketon.ai.tool.result.ToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupportAnswerService {

    private static final String LOGIN_REQUIRED_MESSAGE =
            ToolFailureCode.AUTH_REQUIRED.getSafeMessage();
    private static final String UNSUPPORTED_WRITE_MESSAGE =
            "현재는 조회와 예상 결과 안내만 가능하며 실제 취소나 변경은 수행할 수 없습니다.";
    private static final String OUT_OF_SCOPE_MESSAGE =
            "TicketOn 이용, 예매, 결제, 취소 및 환불 관련 질문을 도와드릴 수 있습니다.";
    private static final String EMPTY_RESERVATIONS_MESSAGE =
            "현재 확인할 수 있는 예매가 없습니다.";

    private static final String DATA_ANSWER_PROMPT = """
            당신은 TicketOn 고객지원 상담원입니다.
            제공된 조회 또는 계산 결과만 사용해 질문에 간결한 한국어로 답변하세요.
            결과에 없는 내용은 추측하지 마세요.
            내부 예매 식별자는 사용자에게 노출하지 마세요.
            """;

    private static final String GENERAL_PROMPT = """
            당신은 TicketOn 고객지원 상담원입니다.
            인사나 감사에 한두 문장으로 친절하고 간결하게 답변하세요.
            """;

    private final SupportRouteService supportRouteService;
    private final PolicyAnswerService policyAnswerService;
    private final MyReservationTool myReservationTool;
    private final RefundEstimateTool refundEstimateTool;
    private final ChatClient.Builder chatClientBuilder;
    private final AiStageObservation aiStageObservation;

    public String answer(
            String question,
            Optional<TicketOnAccessToken> accessToken
    ) {
        return aiStageObservation.observe(
                "support-answer",
                () -> answerByRoute(question, accessToken)
        );
    }

    private String answerByRoute(
            String question,
            Optional<TicketOnAccessToken> accessToken
    ) {
        SupportRoute route = supportRouteService.route(question);

        return switch (route) {
            case POLICY -> policyAnswerService.answer(question).answer();
            case PERSONAL_DATA -> answerPersonalData(question, accessToken);
            case REFUND_CALCULATION -> answerRefund(question, accessToken);
            case UNSUPPORTED_WRITE -> UNSUPPORTED_WRITE_MESSAGE;
            case GENERAL -> generateGeneralAnswer(question);
            case OUT_OF_SCOPE -> OUT_OF_SCOPE_MESSAGE;
        };
    }

    private String answerPersonalData(
            String question,
            Optional<TicketOnAccessToken> accessToken
    ) {
        if (accessToken.isEmpty()) {
            return LOGIN_REQUIRED_MESSAGE;
        }

        ToolResult<List<MyReservationSummary>> result =
                myReservationTool.getMyReservations(toolContext(accessToken.get()));

        if (result instanceof ToolResult.Failure<List<MyReservationSummary>> failure) {
            return failure.message();
        }

        List<MyReservationSummary> reservations =
                ((ToolResult.Success<List<MyReservationSummary>>) result).data();
        if (reservations.isEmpty()) {
            return EMPTY_RESERVATIONS_MESSAGE;
        }

        return generateDataAnswer(
                question,
                reservationContext(reservations)
        );
    }

    private String answerRefund(
            String question,
            Optional<TicketOnAccessToken> accessToken
    ) {
        if (accessToken.isEmpty()) {
            return LOGIN_REQUIRED_MESSAGE;
        }

        ToolContext context = toolContext(accessToken.get());
        ToolResult<List<MyReservationSummary>> reservationResult =
                myReservationTool.getMyReservations(context);

        if (reservationResult instanceof ToolResult.Failure<List<MyReservationSummary>> failure) {
            return failure.message();
        }

        List<MyReservationSummary> reservations =
                ((ToolResult.Success<List<MyReservationSummary>>) reservationResult).data();
        if (reservations.isEmpty()) {
            return EMPTY_RESERVATIONS_MESSAGE;
        }
        if (reservations.size() > 1) {
            return reservationSelectionMessage(reservations);
        }

        ToolResult<RefundEstimate> refundResult = refundEstimateTool.estimateRefund(
                reservations.getFirst().reservationId(),
                context
        );
        if (refundResult instanceof ToolResult.Failure<RefundEstimate> failure) {
            return failure.message();
        }

        RefundEstimate estimate =
                ((ToolResult.Success<RefundEstimate>) refundResult).data();
        return generateDataAnswer(question, refundContext(estimate));
    }

    private ToolContext toolContext(TicketOnAccessToken accessToken) {
        return new ToolContext(Map.of(
                MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY,
                accessToken
        ));
    }

    private String reservationContext(List<MyReservationSummary> reservations) {
        return reservations.stream()
                .map(reservation -> """
                        공연명: %s
                        공연 일시: %s
                        예매 상태: %s
                        예매 일시: %s
                        """.formatted(
                        reservation.eventTitle(),
                        reservation.performanceAt(),
                        reservation.reservationStatus(),
                        reservation.reservedAt()
                ))
                .reduce("", (left, right) -> left + "\n" + right);
    }

    private String refundContext(RefundEstimate estimate) {
        return """
                취소 가능 여부: %s
                공연까지 남은 일수: %d
                취소 수수료율: %d%%
                취소 수수료: %d원
                예상 환불액: %d원
                판단 이유: %s
                """.formatted(
                estimate.cancellationAllowed(),
                estimate.daysUntilPerformance(),
                estimate.feeRate(),
                estimate.feeAmount(),
                estimate.refundAmount(),
                estimate.reason()
        );
    }

    private String reservationSelectionMessage(
            List<MyReservationSummary> reservations
    ) {
        String eventTitles = reservations.stream()
                .map(MyReservationSummary::eventTitle)
                .distinct()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");

        return "환불액을 계산할 공연을 알려주세요: " + eventTitles;
    }

    private String generateDataAnswer(String question, String data) {
        return chatClientBuilder.build()
                .prompt()
                .system(DATA_ANSWER_PROMPT)
                .user("""
                        [사용자 질문]
                        %s

                        [확인된 결과]
                        %s
                        """.formatted(question, data))
                .options(OllamaChatOptions.builder().disableThinking())
                .call()
                .content();
    }

    private String generateGeneralAnswer(String question) {
        return chatClientBuilder.build()
                .prompt()
                .system(GENERAL_PROMPT)
                .user(question)
                .options(OllamaChatOptions.builder().disableThinking())
                .call()
                .content();
    }
}
