package com.ticketon.ai.reservation.service;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MyReservationToolCallingService {

    private static final String SYSTEM_PROMPT = """
            당신은 TicketOn 고객지원 상담원입니다.

            사용자가 자신의 실제 예매 목록, 예매 상태 또는 공연 일정을 물으면
            반드시 getMyReservations Tool을 사용하세요.

            Tool 결과에 없는 정보는 추측하지 마세요.
            여러 예매 중 사용자가 말한 대상을 확정할 수 없으면 임의로 선택하지 말고
            어떤 공연인지 다시 질문하세요.
            실제 예매 취소나 상태 변경을 실행할 수 있다고 말하지 마세요.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final MyReservationTool myReservationTool;

    public String answer(String question, TicketOnAccessToken accessToken) {
        return chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .tools(myReservationTool)
                .toolContext(Map.of(
                        MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY,
                        accessToken
                ))
                .options(OllamaChatOptions.builder().disableThinking())
                .call()
                .content();
    }
}
