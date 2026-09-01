package com.ticketon.ai.refund;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class RefundCalculator {

    private static final int FREE_CANCELLATION_DAYS = 14;
    private static final int TEN_PERCENT_MIN_DAYS = 7;
    private static final int TWENTY_PERCENT_MIN_DAYS = 3;
    private static final int CANCELLATION_MIN_DAYS = 1;
    private static final int FREE_EXCEPTION_MIN_DAYS = 3;
    private static final int FREE_EXCEPTION_HOURS = 24;

    public RefundEstimate calculate(
            RefundSnapshot snapshot,
            LocalDateTime requestedAt
    ) {
        long daysUntilPerformance = ChronoUnit.DAYS.between(
                requestedAt.toLocalDate(),
                snapshot.performanceAt().toLocalDate()
        );

        if (daysUntilPerformance < CANCELLATION_MIN_DAYS) {
            return new RefundEstimate(
                    snapshot.reservationId(),
                    false,
                    daysUntilPerformance,
                    0,
                    0,
                    0,
                    List.of("REFUND-01", "REFUND-02"),
                    "공연 당일 또는 공연 시작 이후에는 취소할 수 없습니다."
            );
        }

        if (isFreeCancellationException(
                snapshot,
                requestedAt,
                daysUntilPerformance
        )) {
            return createEstimate(
                    snapshot,
                    daysUntilPerformance,
                    0,
                    List.of("REFUND-03", "REFUND-04"),
                    "예매 후 24시간 이내이며 공연일까지 3일 이상 남았습니다."
            );
        }

        int feeRate = determineFeeRate(daysUntilPerformance);

        return createEstimate(
                snapshot,
                daysUntilPerformance,
                feeRate,
                List.of("REFUND-02", "REFUND-04"),
                "공연일까지 남은 날짜에 따른 일반 취소 수수료가 적용됩니다."
        );
    }

    private boolean isFreeCancellationException(
            RefundSnapshot snapshot,
            LocalDateTime requestedAt,
            long daysUntilPerformance
    ) {
        boolean withinTwentyFourHours = !requestedAt.isAfter(
                snapshot.reservedAt().plusHours(FREE_EXCEPTION_HOURS)
        );

        return withinTwentyFourHours
                && daysUntilPerformance >= FREE_EXCEPTION_MIN_DAYS;
    }

    private int determineFeeRate(long daysUntilPerformance) {
        if (daysUntilPerformance >= FREE_CANCELLATION_DAYS) {
            return 0;
        }

        if (daysUntilPerformance >= TEN_PERCENT_MIN_DAYS) {
            return 10;
        }

        if (daysUntilPerformance >= TWENTY_PERCENT_MIN_DAYS) {
            return 20;
        }

        return 30;
    }

    private RefundEstimate createEstimate(
            RefundSnapshot snapshot,
            long daysUntilPerformance,
            int feeRate,
            List<String> policyIds,
            String reason
    ) {
        int feeAmount = snapshot.paidAmount() * feeRate / 100;
        int refundAmount = snapshot.paidAmount() - feeAmount;

        return new RefundEstimate(
                snapshot.reservationId(),
                true,
                daysUntilPerformance,
                feeRate,
                feeAmount,
                refundAmount,
                policyIds,
                reason
        );
    }
}
