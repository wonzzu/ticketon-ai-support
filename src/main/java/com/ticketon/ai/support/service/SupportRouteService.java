package com.ticketon.ai.support.service;

import com.ticketon.ai.observation.AiStageObservation;
import com.ticketon.ai.support.domain.SupportRoute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupportRouteService {

    private static final String ROUTER_PROMPT = """
            # 역할

            TicketOn 고객지원 질문에 답하기 위해 필요한 처리 경로를 하나만 선택하세요.
            질문에 포함된 단어가 아니라 필요한 정보와 사용자가 요청한 행동을 기준으로 판단하세요.

            # 처리 경로

            POLICY
            개인 예매 정보를 조회하지 않고 TicketOn의 일반 정책, 이용 방법,
            제한 조건, 처리 기간 또는 오류 원인으로 답할 수 있는 질문입니다.

            PERSONAL_DATA
            사용자의 실제 예매 목록, 공연 일정, 결제 상태 또는
            취소 처리 상태를 조회해야 답할 수 있는 질문입니다.

            REFUND_CALCULATION
            사용자의 실제 예매 기록을 기준으로 취소 가능 여부,
            취소 수수료 또는 예상 환불액을 계산해야 하는 질문입니다.

            UNSUPPORTED_WRITE
            예매 취소, 복구, 변경, 삭제, 등록 또는 승인처럼
            실제 상태를 지금 변경해 달라는 요청입니다.

            GENERAL
            인사, 감사, 작별 또는 TicketOn 상담 범위를 묻는 짧은 대화입니다.

            OUT_OF_SCOPE
            TicketOn 이용과 관계없는 정보 요청입니다.

            # 판단 우선순위

            다음 순서대로 판단하세요.

            TicketOn의 예매, 결제, 좌석, 대기열, 공연, 회원, 판매자,
            후기, 쿠폰과 정산에 관한 질문은 TicketOn 이용 질문입니다.

            1. 실제 상태를 변경해 달라는 명시적인 요청이 포함되면
               UNSUPPORTED_WRITE입니다.
            2. 실제 예매로 취소 가능 여부, 수수료 또는 환불액을 계산해야 하면
               REFUND_CALCULATION입니다.
            3. 실제 예매, 결제 또는 취소 처리 상태를 조회해야 하면
               PERSONAL_DATA입니다.
            4. 개인 데이터 없이 일반 규칙이나 방법으로 답할 수 있으면
               POLICY입니다.
            5. 인사, 감사, 작별 또는 상담 범위 질문이면 GENERAL입니다.
            6. TicketOn과 관계없으면 OUT_OF_SCOPE입니다.

            # 모호한 질문 처리

            - 취소하거나 변경하고 싶다는 의사만 표현하고,
              실제 실행이나 개인 예매 계산을 명확히 요청하지 않았다면
              POLICY로 분류하세요.
            - "할 수 있나요", "가능한가요"처럼 허용 여부를 묻는 질문은
              실제 처리를 명령한 것이 아니므로 POLICY로 분류하세요.
            - "할 수 있나요", "해도 되나요", "가능?", "ㄱㄴ?"은
              삭제, 변경, 결제 같은 행동을 언급해도 실행 명령이 아니라 가능 여부 질문입니다.
            - 질문에 "내"가 있다는 이유만으로 개인 데이터 경로를 선택하지 마세요.
              실제 기록이 있어야 답할 수 있는지를 판단하세요.
            - 실제 예매 중 취소 가능한 대상을 찾으려면 취소 정책 적용이 필요하므로
              REFUND_CALCULATION으로 분류하세요.
            - 띄어쓰기나 맞춤법이 부족한 구어체도 의미가 같으면 동일한 경로로 분류하세요.
            - 일반적인 환불 반영 기간은 POLICY입니다.
            - 사용자의 실제 환불 처리 여부를 확인해야 하면 PERSONAL_DATA입니다.
            - 계산과 상태 변경을 함께 요청하면 상태 변경 요청을 우선해
              UNSUPPORTED_WRITE로 분류하세요.
            - 로그인 여부는 고려하지 말고 질문의 처리 경로만 선택하세요.

            # 경계 예시

            "좌석 중 한 장만 취소할 수 있나요?"
            → POLICY

            "좌석 중 한 장만 취소하고 싶어요."
            → POLICY

            "좌석 여러 장을 함께 예매했는데 그중 한 장만 따로 취소하고 싶어요."
            → POLICY

            "한 장을 취소하면 제 예매에서 얼마를 환불받나요?"
            → REFUND_CALCULATION

            "한 장 지금 취소 처리해 주세요."
            → UNSUPPORTED_WRITE

            "다른 사용자의 후기를 삭제할 수 있나요?"
            → POLICY

            "다른 사용자의 후기를 지금 삭제해 주세요."
            → UNSUPPORTED_WRITE

            "취소 후 카드 환불은 보통 언제 반영되나요?"
            → POLICY

            "제 카드 환불이 실제로 처리됐는지 확인해 주세요."
            → PERSONAL_DATA

            "제 예매를 취소하면 실제 수수료가 얼마인가요?"
            → REFUND_CALCULATION

            "이번 달에 내가 가진 표가 무엇인지 알려주세요."
            → PERSONAL_DATA

            "내 예매 중 실제로 취소 가능한 예매가 무엇인지 알려주세요."
            → REFUND_CALCULATION

            "환불액을 알려주고 바로 취소해 주세요."
            → UNSUPPORTED_WRITE

            # 출력

            POLICY, PERSONAL_DATA, REFUND_CALCULATION, UNSUPPORTED_WRITE,
            GENERAL, OUT_OF_SCOPE 중 하나만 출력하세요.
            설명, 문장부호, Markdown 또는 다른 문자열을 출력하지 마세요.
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

        if (content == null || content.isBlank()) {
            log.warn("지원 경로 분류 결과가 비어 있어 안전한 경로로 처리합니다.");
            return SupportRoute.OUT_OF_SCOPE;
        }

        String route = content.strip()
                .replace("`", "")
                .toUpperCase(Locale.ROOT);

        try {
            return SupportRoute.valueOf(route);
        } catch (IllegalArgumentException exception) {
            log.warn("지원 경로 분류 결과가 허용된 경로와 일치하지 않아 안전한 경로로 처리합니다.");
            return SupportRoute.OUT_OF_SCOPE;
        }
    }
}
