package com.ticketon.ai.refund;

import java.util.List;

public record RefundEstimate(
        Long reservationId,
        boolean cancellationAllowed,
        long daysUntilPerformance,
        int feeRate,
        int feeAmount,
        int refundAmount,
        List<String> appliedPolicyIds,
        String reason
) {
}
