package com.ticketon.ai.reservation.dto;

import java.time.DayOfWeek;

public record ReservationSelectionCriteria(
        String eventTitle,
        Period period,
        DayOfWeek dayOfWeek,
        boolean futureOnly,
        SortField sortField,
        SortDirection sortDirection,
        String reservationStatus,
        boolean firstOnly
) {

    public static ReservationSelectionCriteria thisWeek() {
        return new ReservationSelectionCriteria(
                null,
                Period.THIS_WEEK,
                null,
                false,
                null,
                null,
                null,
                false
        );
    }

    public static ReservationSelectionCriteria nearestUpcoming() {
        return new ReservationSelectionCriteria(
                null,
                Period.NONE,
                null,
                true,
                SortField.PERFORMANCE_AT,
                SortDirection.ASC,
                null,
                true
        );
    }

    public static ReservationSelectionCriteria dayOfWeek(DayOfWeek dayOfWeek) {
        return new ReservationSelectionCriteria(
                null,
                Period.NONE,
                dayOfWeek,
                false,
                null,
                null,
                null,
                false
        );
    }

    public static ReservationSelectionCriteria latestReservation() {
        return new ReservationSelectionCriteria(
                null,
                Period.NONE,
                null,
                false,
                SortField.RESERVED_AT,
                SortDirection.DESC,
                null,
                true
        );
    }

    public enum Period {
        NONE,
        THIS_WEEK
    }

    public enum SortField {
        PERFORMANCE_AT,
        RESERVED_AT
    }

    public enum SortDirection {
        ASC,
        DESC
    }
}
