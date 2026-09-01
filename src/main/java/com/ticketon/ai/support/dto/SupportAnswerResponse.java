package com.ticketon.ai.support.dto;

public record SupportAnswerResponse(String answer) {

    public static SupportAnswerResponse from(String answer) {
        return new SupportAnswerResponse(answer);
    }
}
