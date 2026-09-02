package com.ticketon.ai.support.service;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.policy.tool.PolicySearchTool;
import com.ticketon.ai.refund.tool.RefundEstimateTool;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import com.ticketon.ai.support.tool.LoginRequiredTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupportAnswerService {

    private static final String SYSTEM_PROMPT = """
            당신은 TicketOn 고객지원 상담원입니다.

            일반 정책, 이용 방법 또는 제한 조건이 필요한 질문에는 searchPolicies Tool을 사용하세요.
            searchPolicies Tool 결과의 sufficient가 false라면 정책을 추측하지 말고
            "제공된 정책만으로 확인할 수 없습니다."라고 안내하세요.
            sufficient가 true일 때만 반환된 policies를 근거로 답변하세요.
            로그인한 사용자의 실제 예매 목록, 공연 일정 또는 예매 상태가 필요한 질문에는
            getMyReservations Tool을 사용하세요.
            특정 예매의 취소 가능 여부, 취소 수수료 또는 예상 환불액을 묻는 경우에는
            먼저 getMyReservations Tool로 대상 예매를 확인한 뒤 estimateRefund Tool을 사용하세요.
            사용자에게 예매 식별자를 직접 물어보지 마세요.
            정책과 개인 예매 정보가 모두 필요하면 필요한 Tool을 모두 사용하세요.

            requestLoginForPersonalSupport Tool이 제공된 요청에서는 사용자가 로그인하지 않은 상태입니다.
            이때 사용자가 자신의 실제 예매, 결제 상태 또는 자신이 받을 환불액을 조회해 달라고 하면
            해당 Tool을 사용하세요.
            사용자의 실제 예매 데이터 없이 날짜, 기간, 비율 같은 조건만으로 답할 수 있는 질문은
            로그인 여부와 관계없이 일반 정책 질문이므로 searchPolicies Tool을 사용하세요.

            로그인 정보가 없어 개인 예매를 조회할 수 없으면 추측하지 말고 로그인이 필요하다고 안내하세요.
            Tool 결과에 없는 정보는 추측하지 마세요.
            여러 예매 중 사용자가 말한 대상을 확정할 수 없으면 임의로 선택하지 말고 다시 질문하세요.
            실제 예매 취소나 상태 변경을 실행할 수 있다고 말하지 마세요.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final PolicySearchTool policySearchTool;
    private final MyReservationTool myReservationTool;
    private final RefundEstimateTool refundEstimateTool;
    private final LoginRequiredTool loginRequiredTool;

    public String answer(
            String question,
            Optional<TicketOnAccessToken> accessToken
    ) {
        ChatClient.ChatClientRequestSpec request = chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(question);

        configureTools(request, accessToken);

        return request
                .options(OllamaChatOptions.builder().disableThinking())
                .call()
                .content();
    }

    private void configureTools(
            ChatClient.ChatClientRequestSpec request,
            Optional<TicketOnAccessToken> accessToken
    ) {
        if (accessToken.isEmpty()) {
            request.tools(policySearchTool, loginRequiredTool);
            return;
        }

        request.tools(
                        policySearchTool,
                        myReservationTool,
                        refundEstimateTool
                )
                .toolContext(Map.of(
                        MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY,
                        accessToken.get()
                ));
    }
}
