package com.ticketon.ai.support.service;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.policy.tool.PolicySearchTool;
import com.ticketon.ai.refund.tool.RefundEstimateTool;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import com.ticketon.ai.support.tool.LoginRequiredTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SupportAnswerServiceTest {

    private final ChatClient.Builder builder = mock(ChatClient.Builder.class);
    private final ChatClient chatClient = mock(ChatClient.class);
    private final ChatClient.ChatClientRequestSpec requestSpec =
            mock(ChatClient.ChatClientRequestSpec.class);
    private final ChatClient.CallResponseSpec responseSpec =
            mock(ChatClient.CallResponseSpec.class);
    private final PolicySearchTool policySearchTool = mock(PolicySearchTool.class);
    private final MyReservationTool myReservationTool = mock(MyReservationTool.class);
    private final RefundEstimateTool refundEstimateTool = mock(RefundEstimateTool.class);
    private final LoginRequiredTool loginRequiredTool = mock(LoginRequiredTool.class);

    private SupportAnswerService service;

    @BeforeEach
    void setUp() {
        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.tools(any(Object[].class))).thenReturn(requestSpec);
        when(requestSpec.toolContext(any())).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("고객지원 답변");

        service = new SupportAnswerService(
                builder,
                policySearchTool,
                myReservationTool,
                refundEstimateTool,
                loginRequiredTool
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void JWT가_없으면_정책_Tool만_제공한다() {
        String result = service.answer("취소 정책 알려줘.", Optional.empty());

        verify(requestSpec).tools(policySearchTool, loginRequiredTool);
        verify(requestSpec, never()).toolContext(any());
        assertThat(result).isEqualTo("고객지원 답변");
    }

    @Test
    @SuppressWarnings("unchecked")
    void JWT가_있으면_LLM_문장이_아니라_ToolContext에만_전달한다() {
        TicketOnAccessToken token = new TicketOnAccessToken("secret-user-token");

        service.answer("내 예매 보여줘.", Optional.of(token));

        ArgumentCaptor<Map<String, Object>> contextCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(requestSpec).user("내 예매 보여줘.");
        verify(requestSpec).tools(
                policySearchTool,
                myReservationTool,
                refundEstimateTool
        );
        verify(requestSpec).toolContext(contextCaptor.capture());
        assertThat(contextCaptor.getValue())
                .containsEntry(MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY, token);
    }
}
