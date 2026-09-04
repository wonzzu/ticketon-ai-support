package com.ticketon.ai.support.service;

import com.ticketon.ai.observation.AiStageObservation;
import com.ticketon.ai.support.domain.SupportRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SupportRouteService {

    private static final String ROUTER_PROMPT = """
            TicketOn 고객지원 질문의 처리 경로를 하나만 선택하세요.

            POLICY: 일반 정책, 이용 방법, 제한 조건, 오류 원인
            PERSONAL_DATA: 사용자의 실제 예매 목록, 일정, 결제 또는 예매 상태 조회
            REFUND_CALCULATION: 사용자의 특정 예매에 대한 실제 취소 가능 여부, 수수료, 환불액 계산
            UNSUPPORTED_WRITE: 취소, 복구, 변경, 삭제, 등록, 승인처럼 실제 상태를 바꾸라는 요청
            GENERAL: 인사, 감사, 짧은 일상 대화
            OUT_OF_SCOPE: TicketOn과 관계없는 정보 요청

            다음 순서로 질문의 의도를 판단하세요.
            1. 인사나 감사라면 GENERAL입니다.
            2. TicketOn과 관계없다면 OUT_OF_SCOPE입니다.
            3. 실제 상태를 지금 변경하거나 처리하라는 명령이면 UNSUPPORTED_WRITE입니다.
            4. 사용자의 실제 예매에 취소 정책을 적용해야 하면 REFUND_CALCULATION입니다.
            5. 사용자의 실제 기록만 조회하면 PERSONAL_DATA입니다.
            6. 그 밖의 이용 방법, 규칙, 가능 여부, 오류 원인 질문은 POLICY입니다.

            의미를 구분하는 예시:
            - 어떤 작업이 가능한지 묻는 것은 정책 질문입니다.
            - 그 작업을 지금 수행해 달라는 것은 실제 변경 요청입니다.
            - 일반적인 취소 기준을 묻는 것은 정책 질문입니다.
            - 내 실제 예매의 취소 가능 여부나 환불액을 묻는 것은 환불 계산 질문입니다.
            - 내 실제 예매를 지금 취소해 달라는 것은 실제 변경 요청입니다.

            TicketOn 맥락의 표, 자리, 줄, 카드, 공연, 예매 관련 표현도 TicketOn 질문으로 판단하세요.
            가능한지, 왜 그런지, 어디서 하는지 묻는 질문은 실제 실행 요청이 아니라 POLICY입니다.
            취소해줘, 바꿔줘, 삭제해줘처럼 지금 실제 처리를 명령할 때만 UNSUPPORTED_WRITE입니다.
            가정한 날짜, 기간, 금액으로 환불 규칙을 묻는 질문은 POLICY입니다.
            내 실제 예매를 기준으로 환불액을 계산해 달라는 질문만 REFUND_CALCULATION입니다.
            PERSONAL_DATA는 실제 사용자 기록을 조회해야만 답할 수 있는 질문입니다.
            GENERAL은 인사, 감사, 작별 같은 짧은 대화에만 사용하세요.
            TicketOn과 무관한 설명, 추천 또는 정보 요청은 OUT_OF_SCOPE입니다.
            로그인 여부와 관계없이 질문의 의도만 분류하세요.
            설명하지 말고 경로 이름 하나만 출력하세요.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final AiStageObservation aiStageObservation;

    public SupportRoute route(String question) {
        return aiStageObservation.observe(
                "support-route",
                () -> classify(question)
        );
    }

    private SupportRoute classify(String question) {
        String content = chatClientBuilder.build()
                .prompt()
                .system(ROUTER_PROMPT)
                .user(question)
                .options(OllamaChatOptions.builder().disableThinking())
                .call()
                .content();

        if (content == null) {
            throw new IllegalStateException("지원 경로 분류 결과가 비어 있습니다.");
        }

        String route = content.strip()
                .replace("`", "")
                .toUpperCase(Locale.ROOT);

        try {
            return SupportRoute.valueOf(route);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "지원 경로 분류 결과가 올바르지 않습니다: " + content,
                    exception
            );
        }
    }
}
