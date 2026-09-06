package com.ticketon.ai.reservation.dto;

import java.util.List;

public record ReservationSelectionResult(
        List<MyReservationSummary> reservations,
        boolean criteriaApplied
) {
}
