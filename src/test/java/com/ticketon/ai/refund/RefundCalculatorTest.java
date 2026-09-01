package com.ticketon.ai.refund;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RefundCalculatorTest {

    private static final LocalDateTime REQUESTED_AT =
            LocalDateTime.of(2026, 9, 1, 12, 0);
    private static final int PAID_AMOUNT = 10_001;

    private final RefundCalculator calculator = new RefundCalculator();

    @ParameterizedTest
    @CsvSource({
            "14, 0, 0, 10001",
            "13, 10, 1000, 9001",
            "7, 10, 1000, 9001",
            "6, 20, 2000, 8001",
            "3, 20, 2000, 8001",
            "2, 30, 3000, 7001",
            "1, 30, 3000, 7001"
    })
    void 공연일까지_남은_날짜로_수수료와_환불액을_계산한다(
            long daysUntilPerformance,
            int expectedFeeRate,
            int expectedFeeAmount,
            int expectedRefundAmount
    ) {
        RefundSnapshot snapshot = createSnapshot(
                REQUESTED_AT.minusDays(3),
                REQUESTED_AT.plusDays(daysUntilPerformance)
        );

        RefundEstimate result = calculator.calculate(snapshot, REQUESTED_AT);

        assertThat(result.cancellationAllowed()).isTrue();
        assertThat(result.daysUntilPerformance()).isEqualTo(daysUntilPerformance);
        assertThat(result.feeRate()).isEqualTo(expectedFeeRate);
        assertThat(result.feeAmount()).isEqualTo(expectedFeeAmount);
        assertThat(result.refundAmount()).isEqualTo(expectedRefundAmount);
    }

    @Test
    void 공연_당일에는_취소할_수_없다() {
        RefundSnapshot snapshot = createSnapshot(
                REQUESTED_AT.minusDays(3),
                REQUESTED_AT.plusHours(6)
        );

        RefundEstimate result = calculator.calculate(snapshot, REQUESTED_AT);

        assertThat(result.cancellationAllowed()).isFalse();
        assertThat(result.appliedPolicyIds())
                .containsExactly("REFUND-01", "REFUND-02");
    }

    @Test
    void 예매_후_정확히_24시간이고_공연이_3일_남으면_수수료가_없다() {
        RefundSnapshot snapshot = createSnapshot(
                REQUESTED_AT.minusHours(24),
                REQUESTED_AT.plusDays(3)
        );

        RefundEstimate result = calculator.calculate(snapshot, REQUESTED_AT);

        assertThat(result.feeRate()).isZero();
        assertThat(result.feeAmount()).isZero();
        assertThat(result.refundAmount()).isEqualTo(PAID_AMOUNT);
        assertThat(result.appliedPolicyIds())
                .containsExactly("REFUND-03", "REFUND-04");
    }

    @Test
    void 예매_후_24시간을_지나면_일반_수수료를_적용한다() {
        RefundSnapshot snapshot = createSnapshot(
                REQUESTED_AT.minusHours(24).minusSeconds(1),
                REQUESTED_AT.plusDays(3)
        );

        RefundEstimate result = calculator.calculate(snapshot, REQUESTED_AT);

        assertThat(result.feeRate()).isEqualTo(20);
        assertThat(result.feeAmount()).isEqualTo(2_000);
        assertThat(result.refundAmount()).isEqualTo(8_001);
    }

    @Test
    void 공연이_2일_남으면_예매_후_24시간_예외를_적용하지_않는다() {
        RefundSnapshot snapshot = createSnapshot(
                REQUESTED_AT.minusHours(1),
                REQUESTED_AT.plusDays(2)
        );

        RefundEstimate result = calculator.calculate(snapshot, REQUESTED_AT);

        assertThat(result.feeRate()).isEqualTo(30);
        assertThat(result.feeAmount()).isEqualTo(3_000);
        assertThat(result.refundAmount()).isEqualTo(7_001);
    }

    private RefundSnapshot createSnapshot(
            LocalDateTime reservedAt,
            LocalDateTime performanceAt
    ) {
        return new RefundSnapshot(
                10L,
                "CONFIRMED",
                reservedAt,
                performanceAt,
                PAID_AMOUNT,
                "PAID"
        );
    }
}
