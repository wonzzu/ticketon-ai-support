package com.ticketon.ai.auth;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

public record TicketOnAccessToken(String value) {

    private static final String BEARER_PREFIX = "Bearer ";

    public static Optional<TicketOnAccessToken> fromOptional(
            String authorizationHeader
    ) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return Optional.empty();
        }

        return Optional.of(from(authorizationHeader));
    }

    public static TicketOnAccessToken from(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "TicketOn 로그인이 필요합니다."
            );
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        if (!StringUtils.hasText(token)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "TicketOn 로그인이 필요합니다."
            );
        }

        return new TicketOnAccessToken(token);
    }
}
