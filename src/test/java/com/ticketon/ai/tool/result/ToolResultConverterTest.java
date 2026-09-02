package com.ticketon.ai.tool.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToolResultConverterTest {

    private final ToolResultConverter converter = new ToolResultConverter();

    @Test
    void 성공_결과는_LLM에_전달할_JSON으로_변환한다() {
        String result = converter.convert(
                ToolResult.success("예매 조회 성공"),
                ToolResult.class
        );

        assertThat(result)
                .contains("data")
                .contains("예매 조회 성공");
    }

    @Test
    void 실패_결과는_LLM에_전달하지_않고_예외로_변환한다() {
        ToolResult<Object> failure =
                ToolResult.failure(ToolFailureCode.FORBIDDEN);

        assertThatThrownBy(() -> converter.convert(failure, ToolResult.class))
                .isInstanceOfSatisfying(
                        ToolFailureException.class,
                        exception -> assertThat(exception.getFailureCode())
                                .isEqualTo(ToolFailureCode.FORBIDDEN)
                );
    }
}
