package com.ticketon.ai.tool.result;

public sealed interface ToolResult<T>
        permits ToolResult.Success, ToolResult.Failure {

    static <T> ToolResult<T> success(T data) {
        return new Success<>(data);
    }

    static <T> ToolResult<T> failure(ToolFailureCode code) {
        return new Failure<>(code, code.getSafeMessage());
    }

    record Success<T>(T data) implements ToolResult<T> {
    }

    record Failure<T>(
            ToolFailureCode code,
            String message
    ) implements ToolResult<T> {
    }
}
