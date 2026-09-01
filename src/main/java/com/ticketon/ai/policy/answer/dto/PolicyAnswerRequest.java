package com.ticketon.ai.policy.answer.dto;

import jakarta.validation.constraints.NotBlank;

public record PolicyAnswerRequest(@NotBlank(message = "질문은 필수입니다.") String question) {}