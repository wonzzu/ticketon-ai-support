package com.ticketon.ai.client;

import com.ticketon.ai.tool.result.ToolFailureCode;
import lombok.Getter;

@Getter
public class TicketOnClientException extends RuntimeException {

    private final ToolFailureCode failureCode;

    public TicketOnClientException(ToolFailureCode failureCode) {
        super(failureCode.getSafeMessage());
        this.failureCode = failureCode;
    }

    public TicketOnClientException(
            ToolFailureCode failureCode,
            Throwable cause
    ) {
        super(failureCode.getSafeMessage(), cause);
        this.failureCode = failureCode;
    }
}
