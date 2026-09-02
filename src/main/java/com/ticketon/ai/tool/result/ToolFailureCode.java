package com.ticketon.ai.tool.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ToolFailureCode {

    AUTH_REQUIRED("로그인이 필요하거나 인증 정보가 유효하지 않습니다."),
    FORBIDDEN("해당 정보에 접근할 수 없습니다."),
    NOT_FOUND("요청한 예매 정보를 확인할 수 없습니다."),
    TIMEOUT("현재 예매 정보를 확인하는 데 시간이 오래 걸리고 있습니다."),
    UPSTREAM_UNAVAILABLE("현재 예매 서비스에 연결할 수 없습니다."),
    INVALID_RESPONSE("현재 예매 정보를 정상적으로 확인할 수 없습니다.");

    private final String safeMessage;
}
