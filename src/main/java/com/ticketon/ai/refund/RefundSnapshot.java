package com.ticketon.ai.refund;

import java.time.LocalDateTime;

public record RefundSnapshot(
        Long reservationId,
        String reservationStatus,
        LocalDateTime reservedAt,
        LocalDateTime performanceAt,
        Integer paidAmount,
        String paymentStatus
) {
}