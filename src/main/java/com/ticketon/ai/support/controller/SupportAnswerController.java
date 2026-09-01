package com.ticketon.ai.support.controller;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.policy.answer.dto.PolicyAnswerRequest;
import com.ticketon.ai.support.dto.SupportAnswerResponse;
import com.ticketon.ai.support.service.SupportAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support/answers")
public class SupportAnswerController {

    private final SupportAnswerService supportAnswerService;

    @PostMapping
    public SupportAnswerResponse answer(
            @Valid @RequestBody PolicyAnswerRequest request,
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            ) String authorizationHeader
    ) {
        Optional<TicketOnAccessToken> accessToken =
                TicketOnAccessToken.fromOptional(authorizationHeader);
        String answer = supportAnswerService.answer(
                request.question(),
                accessToken
        );

        return SupportAnswerResponse.from(answer);
    }
}
