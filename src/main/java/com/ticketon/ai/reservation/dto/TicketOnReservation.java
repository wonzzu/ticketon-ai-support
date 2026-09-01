package com.ticketon.ai.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketOnReservation(
        Long id,
        String eventTitle,
        LocalDateTime showDateTime,
        String status,
        LocalDateTime createdAt
) {
}
