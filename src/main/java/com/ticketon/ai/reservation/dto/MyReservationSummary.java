package com.ticketon.ai.reservation.dto;

import java.time.LocalDateTime;

public record MyReservationSummary(
        Long reservationId,
        String eventTitle,
        LocalDateTime performanceAt,
        String reservationStatus,
        LocalDateTime reservedAt
) {

    public static MyReservationSummary from(TicketOnReservation reservation) {
        return new MyReservationSummary(
                reservation.id(),
                reservation.eventTitle(),
                reservation.showDateTime(),
                reservation.status(),
                reservation.createdAt()
        );
    }
}
