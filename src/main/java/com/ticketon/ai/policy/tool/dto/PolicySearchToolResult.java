package com.ticketon.ai.policy.tool.dto;

import com.ticketon.ai.policy.search.dto.PolicySearchResponse;

import java.util.List;

public record PolicySearchToolResult(
        boolean sufficient,
        String message,
        List<PolicySearchResponse> policies
) {

    public static PolicySearchToolResult sufficient(
            List<PolicySearchResponse> policies
    ) {
        return new PolicySearchToolResult(
                true,
                "검색된 정책으로 질문에 답할 수 있습니다.",
                policies
        );
    }

    public static PolicySearchToolResult insufficient() {
        return new PolicySearchToolResult(
                false,
                "검색된 정책만으로 질문에 답할 수 없습니다.",
                List.of()
        );
    }
}
