package com.ticketon.ai.client;

public record TicketOnResponse<T>(
        boolean success,
        int code,
        String message,
        T data
) {
}