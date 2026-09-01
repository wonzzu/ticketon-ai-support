package com.ticketon.ai.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketOnPage<T>(List<T> content) {
}
