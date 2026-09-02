package com.ticketon.ai.tool.result;

import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.execution.ToolCallResultConverter;

import java.lang.reflect.Type;

public class ToolResultConverter implements ToolCallResultConverter {

    private final DefaultToolCallResultConverter delegate =
            new DefaultToolCallResultConverter();

    @Override
    public String convert(Object result, Type returnType) {
        if (result instanceof ToolResult.Failure<?> failure) {
            throw new ToolFailureException(failure.code());
        }

        return delegate.convert(result, returnType);
    }
}
