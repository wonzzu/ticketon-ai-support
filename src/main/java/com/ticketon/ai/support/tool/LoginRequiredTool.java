package com.ticketon.ai.support.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class LoginRequiredTool {

    @Tool(description = """
            로그인하지 않은 사용자가 '내 예매', '내 결제', '내가 받을 환불액'처럼
            자신의 실제 개인 데이터를 조회해 달라고 요청할 때만 사용합니다.
            날짜나 조건을 가정해 취소 수수료율 또는 환불 정책을 묻는 일반 질문에는 사용하지 말고
            searchPolicies를 사용하세요.
            """)
    public String requestLoginForPersonalSupport() {
        return "개인 예매 정보를 확인하려면 TicketOn 로그인이 필요합니다.";
    }
}
