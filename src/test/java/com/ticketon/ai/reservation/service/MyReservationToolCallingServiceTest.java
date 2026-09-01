package com.ticketon.ai.reservation.service;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MyReservationToolCallingServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void JWT는_사용자_질문이_아니라_ToolContext로만_전달한다() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec =
                mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec =
                mock(ChatClient.CallResponseSpec.class);
        MyReservationTool tool = mock(MyReservationTool.class);

        when(builder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.tools(any(Object[].class))).thenReturn(requestSpec);
        when(requestSpec.toolContext(any())).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("예매 목록 답변");

        MyReservationToolCallingService service =
                new MyReservationToolCallingService(builder, tool);
        TicketOnAccessToken token = new TicketOnAccessToken("secret-user-token");

        String result = service.answer("내 예매 목록 보여줘.", token);

        ArgumentCaptor<Map<String, Object>> contextCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(requestSpec).user("내 예매 목록 보여줘.");
        verify(requestSpec).tools(tool);
        verify(requestSpec).toolContext(contextCaptor.capture());
        assertThat(contextCaptor.getValue())
                .containsEntry(MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY, token);
        assertThat(result).isEqualTo("예매 목록 답변");
    }
}
