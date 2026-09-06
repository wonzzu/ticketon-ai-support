package com.ticketon.ai.reservation.service;

import com.ticketon.ai.observation.AiStageObservation;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.reservation.dto.ReservationSelectionCriteria;
import com.ticketon.ai.reservation.dto.ReservationSelectionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ReservationSelectionService {

    private static final List<String> SELECTION_TERMS = List.of(
            "최근",
            "마지막",
            "방금",
            "가까운",
            "다가오는",
            "다음",
            "이번 주",
            "이번주",
            "처음",
            "오래된",
            "결제 완료",
            "결제 대기",
            "취소된"
    );
    private static final List<String> DAY_OF_WEEK_TERMS = List.of(
            "월요일",
            "화요일",
            "수요일",
            "목요일",
            "금요일",
            "토요일",
            "일요일"
    );
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?:\\d{4}[-./]\\d{1,2}[-./]\\d{1,2})|"
                    + "(?:\\d{1,2}\\s*월\\s*\\d{1,2}\\s*일)"
    );

    private static final String SELECTION_PROMPT = """
            사용자의 질문에서 예매 후보를 찾는 조건만 구조화하세요.
            예매 후보를 직접 선택하거나 날짜를 직접 계산하지 마세요.

            규칙:
            - 이번 주는 period를 THIS_WEEK로 반환하세요.
            - 요일이 있으면 dayOfWeek를 반환하세요.
            - 가장 가까운, 다가오는, 다음 공연은 futureOnly=true,
              sortField=PERFORMANCE_AT, sortDirection=ASC, firstOnly=true로 반환하세요.
            - 최근, 마지막, 방금 산 예매는 sortField=RESERVED_AT,
              sortDirection=DESC, firstOnly=true로 반환하세요.
            - 처음 또는 오래된 예매는 sortField=RESERVED_AT,
              sortDirection=ASC, firstOnly=true로 반환하세요.
            - 결제 완료는 reservationStatus=CONFIRMED,
              결제 대기는 PENDING, 취소된 예매는 CANCEL로 반환하세요.
            - 질문에 공연명이 명시된 경우에만 eventTitle을 반환하세요.
            - 질문에 없는 조건을 추측하지 마세요.
            - 후보 번호와 예매 식별자는 반환하지 마세요.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final AiStageObservation aiStageObservation;
    private final Clock clock;

    public ReservationSelectionResult find(
            String question,
            List<MyReservationSummary> reservations
    ) {
        if (reservations.isEmpty() || !hasSelectionCondition(question, reservations)) {
            return new ReservationSelectionResult(reservations, false);
        }

        ReservationSelectionCriteria criteria = knownCriteria(question, reservations)
                .orElseGet(() -> aiStageObservation.observe(
                        "reservation-selection",
                        () -> extractCriteria(question, reservations)
                ));
        if (criteria == null) {
            return new ReservationSelectionResult(List.of(), true);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        return new ReservationSelectionResult(
                apply(criteria, reservations, now),
                true
        );
    }

    static List<MyReservationSummary> apply(
            ReservationSelectionCriteria criteria,
            List<MyReservationSummary> reservations,
            LocalDateTime now
    ) {
        if (criteria == null) {
            return reservations;
        }

        List<MyReservationSummary> selected = reservations.stream()
                .filter(reservation -> matchesPeriod(criteria, reservation, now))
                .filter(reservation -> matchesDayOfWeek(criteria, reservation))
                .filter(reservation -> matchesFuture(criteria, reservation, now))
                .filter(reservation -> matchesStatus(criteria, reservation))
                .filter(reservation -> matchesTitle(criteria, reservation))
                .sorted(comparator(criteria))
                .toList();

        if (criteria.firstOnly() && !selected.isEmpty()) {
            return List.of(selected.getFirst());
        }

        return selected;
    }

    static boolean hasSelectionCondition(
            String question,
            List<MyReservationSummary> reservations
    ) {
        String normalizedQuestion = normalize(question);
        boolean hasMatchingTitle = reservations.stream()
                .map(MyReservationSummary::eventTitle)
                .map(ReservationSelectionService::normalize)
                .anyMatch(normalizedQuestion::contains);
        if (hasMatchingTitle) {
            return true;
        }

        boolean hasSelectionTerm = SELECTION_TERMS.stream()
                .anyMatch(question::contains);
        boolean hasDayOfWeek = DAY_OF_WEEK_TERMS.stream()
                .anyMatch(question::contains);

        return hasSelectionTerm
                || hasDayOfWeek
                || DATE_PATTERN.matcher(question).find();
    }

    static Optional<ReservationSelectionCriteria> knownCriteria(
            String question,
            List<MyReservationSummary> reservations
    ) {
        ReservationSelectionCriteria.Period period =
                containsAny(question, List.of("이번 주", "이번주"))
                        ? ReservationSelectionCriteria.Period.THIS_WEEK
                        : ReservationSelectionCriteria.Period.NONE;
        DayOfWeek dayOfWeek = extractDayOfWeek(question);

        boolean nearestUpcoming = containsAny(
                question,
                List.of("가장 가까운", "다가오는", "다음 공연")
        );
        boolean latestReservation = containsAny(
                question,
                List.of("최근", "마지막", "방금")
        );
        boolean oldestReservation = containsAny(
                question,
                List.of("처음", "오래된")
        );

        ReservationSelectionCriteria.SortField sortField = null;
        ReservationSelectionCriteria.SortDirection sortDirection = null;
        boolean firstOnly = false;

        if (nearestUpcoming) {
            sortField = ReservationSelectionCriteria.SortField.PERFORMANCE_AT;
            sortDirection = ReservationSelectionCriteria.SortDirection.ASC;
            firstOnly = true;
        } else if (latestReservation) {
            sortField = ReservationSelectionCriteria.SortField.RESERVED_AT;
            sortDirection = ReservationSelectionCriteria.SortDirection.DESC;
            firstOnly = true;
        } else if (oldestReservation) {
            sortField = ReservationSelectionCriteria.SortField.RESERVED_AT;
            sortDirection = ReservationSelectionCriteria.SortDirection.ASC;
            firstOnly = true;
        }

        String reservationStatus = extractReservationStatus(question);
        String eventTitle = extractEventTitle(question, reservations);
        boolean criteriaFound = period != ReservationSelectionCriteria.Period.NONE
                || dayOfWeek != null
                || sortField != null
                || reservationStatus != null
                || eventTitle != null;
        if (!criteriaFound) {
            return Optional.empty();
        }

        return Optional.of(new ReservationSelectionCriteria(
                eventTitle,
                period,
                dayOfWeek,
                nearestUpcoming,
                sortField,
                sortDirection,
                reservationStatus,
                firstOnly
        ));
    }

    private static DayOfWeek extractDayOfWeek(String question) {
        if (question.contains("월요일")) {
            return DayOfWeek.MONDAY;
        }
        if (question.contains("화요일")) {
            return DayOfWeek.TUESDAY;
        }
        if (question.contains("수요일")) {
            return DayOfWeek.WEDNESDAY;
        }
        if (question.contains("목요일")) {
            return DayOfWeek.THURSDAY;
        }
        if (question.contains("금요일")) {
            return DayOfWeek.FRIDAY;
        }
        if (question.contains("토요일")) {
            return DayOfWeek.SATURDAY;
        }
        if (question.contains("일요일")) {
            return DayOfWeek.SUNDAY;
        }

        return null;
    }

    private static String extractReservationStatus(String question) {
        if (containsAny(question, List.of("결제 완료된 것만", "결제 완료된 예매"))) {
            return "CONFIRMED";
        }
        if (containsAny(question, List.of("결제 대기 예매", "결제 대기인 예매"))) {
            return "PENDING";
        }
        if (containsAny(question, List.of("취소된 예매", "취소한 예매"))) {
            return "CANCEL";
        }

        return null;
    }

    private static String extractEventTitle(
            String question,
            List<MyReservationSummary> reservations
    ) {
        String normalizedQuestion = normalize(question);

        return reservations.stream()
                .map(MyReservationSummary::eventTitle)
                .filter(title -> normalizedQuestion.contains(normalize(title)))
                .findFirst()
                .orElse(null);
    }

    private static boolean containsAny(String question, List<String> terms) {
        return terms.stream().anyMatch(question::contains);
    }

    private ReservationSelectionCriteria extractCriteria(
            String question,
            List<MyReservationSummary> reservations
    ) {
        return chatClientBuilder.build()
                .prompt()
                .system(SELECTION_PROMPT)
                .user("""
                        [현재 날짜]
                        %s

                        [사용자 질문]
                        %s

                        [예매 후보]
                        %s
                        """.formatted(
                        LocalDate.now(clock),
                        question,
                        candidateContext(reservations)
                ))
                .options(OllamaChatOptions.builder().disableThinking())
                .call()
                .entity(
                        ReservationSelectionCriteria.class,
                        spec -> spec.useProviderStructuredOutput()
                );
    }

    private String candidateContext(List<MyReservationSummary> reservations) {
        StringBuilder context = new StringBuilder();

        for (MyReservationSummary reservation : reservations) {
            context.append("""
                    공연명: %s
                    공연 일시: %s
                    예매 상태: %s
                    예매 일시: %s

                    """.formatted(
                    reservation.eventTitle(),
                    reservation.performanceAt(),
                    reservation.reservationStatus(),
                    reservation.reservedAt()
            ));
        }

        return context.toString();
    }

    private static boolean matchesPeriod(
            ReservationSelectionCriteria criteria,
            MyReservationSummary reservation,
            LocalDateTime now
    ) {
        if (criteria.period() != ReservationSelectionCriteria.Period.THIS_WEEK) {
            return true;
        }

        LocalDate today = now.toLocalDate();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate nextWeekStart = weekStart.plusWeeks(1);
        LocalDate performanceDate = reservation.performanceAt().toLocalDate();

        return !performanceDate.isBefore(weekStart)
                && performanceDate.isBefore(nextWeekStart);
    }

    private static boolean matchesDayOfWeek(
            ReservationSelectionCriteria criteria,
            MyReservationSummary reservation
    ) {
        return criteria.dayOfWeek() == null
                || reservation.performanceAt().getDayOfWeek() == criteria.dayOfWeek();
    }

    private static boolean matchesFuture(
            ReservationSelectionCriteria criteria,
            MyReservationSummary reservation,
            LocalDateTime now
    ) {
        return !criteria.futureOnly()
                || !reservation.performanceAt().isBefore(now);
    }

    private static boolean matchesStatus(
            ReservationSelectionCriteria criteria,
            MyReservationSummary reservation
    ) {
        return criteria.reservationStatus() == null
                || criteria.reservationStatus().equals(reservation.reservationStatus());
    }

    private static boolean matchesTitle(
            ReservationSelectionCriteria criteria,
            MyReservationSummary reservation
    ) {
        if (criteria.eventTitle() == null || criteria.eventTitle().isBlank()) {
            return true;
        }

        return normalize(reservation.eventTitle())
                .equals(normalize(criteria.eventTitle()));
    }

    private static Comparator<MyReservationSummary> comparator(
            ReservationSelectionCriteria criteria
    ) {
        if (criteria.sortField() == ReservationSelectionCriteria.SortField.PERFORMANCE_AT) {
            return direction(
                    Comparator.comparing(MyReservationSummary::performanceAt),
                    criteria.sortDirection()
            );
        }
        if (criteria.sortField() == ReservationSelectionCriteria.SortField.RESERVED_AT) {
            return direction(
                    Comparator.comparing(MyReservationSummary::reservedAt),
                    criteria.sortDirection()
            );
        }

        return (left, right) -> 0;
    }

    private static Comparator<MyReservationSummary> direction(
            Comparator<MyReservationSummary> comparator,
            ReservationSelectionCriteria.SortDirection direction
    ) {
        return direction == ReservationSelectionCriteria.SortDirection.DESC
                ? comparator.reversed()
                : comparator;
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^0-9a-z가-힣]", "");
    }
}
