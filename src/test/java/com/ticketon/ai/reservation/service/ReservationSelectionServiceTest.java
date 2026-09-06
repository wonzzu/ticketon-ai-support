package com.ticketon.ai.reservation.service;

import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.reservation.dto.ReservationSelectionCriteria;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationSelectionServiceTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 9, 4, 12, 0);

    @Test
    void 이번_주_공연만_선택한다() {
        List<MyReservationSummary> reservations = List.of(
                reservation(11L, "이번 주 공연", "2026-09-05T19:00", "2026-08-01T10:00"),
                reservation(22L, "다음 주 공연", "2026-09-07T19:00", "2026-08-02T10:00")
        );
        ReservationSelectionCriteria criteria =
                ReservationSelectionCriteria.thisWeek();

        List<MyReservationSummary> selected =
                ReservationSelectionService.apply(criteria, reservations, NOW);

        assertThat(selected).containsExactly(reservations.getFirst());
    }

    @Test
    void 가장_가까운_미래_공연을_선택한다() {
        List<MyReservationSummary> reservations = List.of(
                reservation(11L, "지난 공연", "2026-09-01T19:00", "2026-08-01T10:00"),
                reservation(22L, "가까운 공연", "2026-09-05T19:00", "2026-08-02T10:00"),
                reservation(33L, "먼 공연", "2026-09-20T19:00", "2026-08-03T10:00")
        );
        ReservationSelectionCriteria criteria =
                ReservationSelectionCriteria.nearestUpcoming();

        List<MyReservationSummary> selected =
                ReservationSelectionService.apply(criteria, reservations, NOW);

        assertThat(selected).containsExactly(reservations.get(1));
    }

    @Test
    void 지정한_요일의_공연만_선택한다() {
        List<MyReservationSummary> reservations = List.of(
                reservation(11L, "토요일 공연", "2026-09-05T19:00", "2026-08-01T10:00"),
                reservation(22L, "일요일 공연", "2026-09-06T19:00", "2026-08-02T10:00")
        );
        ReservationSelectionCriteria criteria =
                ReservationSelectionCriteria.dayOfWeek(DayOfWeek.SATURDAY);

        List<MyReservationSummary> selected =
                ReservationSelectionService.apply(criteria, reservations, NOW);

        assertThat(selected).containsExactly(reservations.getFirst());
    }

    @Test
    void 방금_산_티켓은_예매_일시가_가장_최근인_예매를_선택한다() {
        List<MyReservationSummary> reservations = List.of(
                reservation(11L, "이전 예매", "2026-09-20T19:00", "2026-08-01T10:00"),
                reservation(22L, "최근 예매", "2026-09-10T19:00", "2026-09-04T11:50")
        );
        ReservationSelectionCriteria criteria =
                ReservationSelectionCriteria.latestReservation();

        List<MyReservationSummary> selected =
                ReservationSelectionService.apply(criteria, reservations, NOW);

        assertThat(selected).containsExactly(reservations.get(1));
    }

    @Test
    void 토요일은_LLM을_거치지_않고_요일_조건으로_변환한다() {
        ReservationSelectionCriteria criteria =
                ReservationSelectionService.knownCriteria(
                        "토요일에 보는 공연 취소하면 얼마야?",
                        List.of()
                ).orElseThrow();

        assertThat(criteria.dayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        assertThat(criteria.futureOnly()).isFalse();
    }

    @Test
    void 방금_산_티켓은_LLM을_거치지_않고_최근_예매_조건으로_변환한다() {
        ReservationSelectionCriteria criteria =
                ReservationSelectionService.knownCriteria(
                        "방금 산 티켓이 결제 완료인지 확인해줘.",
                        List.of()
                ).orElseThrow();

        assertThat(criteria.sortField())
                .isEqualTo(ReservationSelectionCriteria.SortField.RESERVED_AT);
        assertThat(criteria.sortDirection())
                .isEqualTo(ReservationSelectionCriteria.SortDirection.DESC);
        assertThat(criteria.firstOnly()).isTrue();
        assertThat(criteria.reservationStatus()).isNull();
    }

    private MyReservationSummary reservation(
            Long reservationId,
            String eventTitle,
            String performanceAt,
            String reservedAt
    ) {
        return new MyReservationSummary(
                reservationId,
                eventTitle,
                LocalDateTime.parse(performanceAt),
                "CONFIRMED",
                LocalDateTime.parse(reservedAt)
        );
    }
}
