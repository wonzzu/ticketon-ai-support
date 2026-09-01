package com.ticketon.ai.support.tool;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequiredToolTest {

    private final LoginRequiredTool tool = new LoginRequiredTool();

    @Test
    void 비로그인_개인정보_질문에_로그인이_필요하다고_안내한다() {
        String result = tool.requestLoginForPersonalSupport();

        assertThat(result)
                .contains("개인 예매 정보")
                .contains("로그인");
    }
}
