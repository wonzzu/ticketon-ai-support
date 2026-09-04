package com.ticketon.ai.reservation.service;

import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.reservation.dto.ReservationSelection;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationSelectionServiceTest {

    private final List<MyReservationSummary> reservations = List.of(
            reservation(11L, "첫 번째 공연"),
            reservation(22L, "두 번째 공연")
    );

    @Test
    void 정상적인_후보_번호에_해당하는_예매를_선택한다() {
        ReservationSelection selection = new ReservationSelection(2);

        assertThat(ReservationSelectionService.resolve(selection, reservations))
                .contains(reservations.get(1));
    }

    @Test
    void 선택이_불명확하면_예매를_선택하지_않는다() {
        ReservationSelection selection = new ReservationSelection(null);

        assertThat(ReservationSelectionService.resolve(selection, reservations))
                .isEmpty();
    }

    @Test
    void 후보_범위를_벗어난_번호는_거부한다() {
        ReservationSelection selection = new ReservationSelection(3);

        assertThat(ReservationSelectionService.resolve(selection, reservations))
                .isEmpty();
    }

    @Test
    void 후보_번호가_없으면_예매를_선택하지_않는다() {
        ReservationSelection selection = new ReservationSelection(null);

        assertThat(ReservationSelectionService.resolve(selection, reservations))
                .isEmpty();
    }

    @Test
    void 후보를_구분할_단서가_없는_질문은_선택하지_않는다() {
        assertThat(ReservationSelectionService.hasSelectionCondition(
                "내 표 하나 취소하면 얼마 받아?",
                reservations
        )).isFalse();
    }

    @Test
    void 최근이라는_시간_조건이_있으면_선택을_시도할_수_있다() {
        assertThat(ReservationSelectionService.hasSelectionCondition(
                "내 가장 최근 예매를 취소하면 얼마 받아?",
                reservations
        )).isTrue();
    }

    @Test
    void 하나의_후보에만_해당하는_공연명이_있으면_선택을_시도할_수_있다() {
        assertThat(ReservationSelectionService.hasSelectionCondition(
                "두 번째 공연을 취소하면 얼마 받아?",
                reservations
        )).isTrue();
    }

    @Test
    void 같은_공연명만으로_여러_후보를_구분할_수_없다() {
        List<MyReservationSummary> sameTitleReservations = List.of(
                reservation(11L, "같은 공연"),
                reservation(22L, "같은 공연")
        );

        assertThat(ReservationSelectionService.hasSelectionCondition(
                "같은 공연을 취소하면 얼마 받아?",
                sameTitleReservations
        )).isFalse();
    }

    private MyReservationSummary reservation(Long reservationId, String eventTitle) {
        return new MyReservationSummary(
                reservationId,
                eventTitle,
                LocalDateTime.of(2026, 9, 20, 19, 0),
                "CONFIRMED",
                LocalDateTime.of(2026, 8, 20, 12, 0)
        );
    }
}
