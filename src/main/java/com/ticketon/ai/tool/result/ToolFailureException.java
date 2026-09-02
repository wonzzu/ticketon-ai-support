package com.ticketon.ai.tool.result;

import lombok.Getter;

@Getter
public class ToolFailureException extends RuntimeException {

    private final ToolFailureCode failureCode;

    public ToolFailureException(ToolFailureCode failureCode) {
        super(failureCode.getSafeMessage());
        this.failureCode = failureCode;
    }
}
