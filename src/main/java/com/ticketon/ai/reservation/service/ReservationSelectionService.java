package com.ticketon.ai.reservation.service;

import com.ticketon.ai.observation.AiStageObservation;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.reservation.dto.ReservationSelection;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ReservationSelectionService {

    private static final List<String> TEMPORAL_SELECTION_TERMS = List.of(
            "최근",
            "마지막",
            "가까운",
            "다가오는",
            "다음",
            "처음",
            "오래된"
    );
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?:\\d{4}[-./]\\d{1,2}[-./]\\d{1,2})|(?:\\d{1,2}\\s*월)|(?:\\d{1,2}\\s*일)"
    );

    private static final String SELECTION_PROMPT = """
            사용자의 질문과 예매 후보를 비교하여 질문이 가리키는 후보 번호를 선택하세요.
            환불 가능 여부를 판단하지 말고 대상 예매만 선택하세요.

            질문에 공연명이 있으면 같은 공연명의 후보를 선택하세요.
            '가장 최근 예매'는 예매 일시가 가장 늦은 후보를 뜻합니다.
            '가장 가까운 공연'은 공연 일시가 가장 이른 미래 후보를 뜻합니다.

            조건에 맞는 후보가 정확히 하나면 candidateNumber를 반환하세요.
            조건에 맞는 후보가 없거나 둘 이상이면 candidateNumber는 null로 반환하세요.
            질문에 공연명, 공연일, 예매 시점처럼 후보를 구분할 조건이 없으면
            임의로 하나를 선택하지 말고 candidateNumber는 null로 반환하세요.
            같은 공연명의 후보가 둘 이상이면 다른 조건으로 하나를 구분할 수 있을 때만 선택하세요.
            후보에 없는 번호나 예매 식별자를 만들지 마세요.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final AiStageObservation aiStageObservation;

    public Optional<MyReservationSummary> select(
            String question,
            List<MyReservationSummary> reservations
    ) {
        if (reservations.size() == 1) {
            return Optional.of(reservations.getFirst());
        }
        if (!hasSelectionCondition(question, reservations)) {
            return Optional.empty();
        }

        ReservationSelection selection = aiStageObservation.observe(
                "reservation-selection",
                () -> selectCandidate(question, reservations)
        );

        return resolve(selection, reservations);
    }

    static boolean hasSelectionCondition(
            String question,
            List<MyReservationSummary> reservations
    ) {
        String normalizedQuestion = normalize(question);
        long matchingTitleCount = reservations.stream()
                .map(MyReservationSummary::eventTitle)
                .map(ReservationSelectionService::normalize)
                .filter(normalizedQuestion::contains)
                .count();
        if (matchingTitleCount == 1) {
            return true;
        }

        boolean hasTemporalCondition = TEMPORAL_SELECTION_TERMS.stream()
                .anyMatch(question::contains);

        return hasTemporalCondition || DATE_PATTERN.matcher(question).find();
    }

    static Optional<MyReservationSummary> resolve(
            ReservationSelection selection,
            List<MyReservationSummary> reservations
    ) {
        if (selection == null || selection.candidateNumber() == null) {
            return Optional.empty();
        }

        int candidateIndex = selection.candidateNumber() - 1;
        if (candidateIndex < 0 || candidateIndex >= reservations.size()) {
            return Optional.empty();
        }

        return Optional.of(reservations.get(candidateIndex));
    }

    private ReservationSelection selectCandidate(
            String question,
            List<MyReservationSummary> reservations
    ) {
        return chatClientBuilder.build()
                .prompt()
                .system(SELECTION_PROMPT)
                .user("""
                        [사용자 질문]
                        %s

                        [예매 후보]
                        %s
                        """.formatted(question, candidateContext(reservations)))
                .options(OllamaChatOptions.builder().disableThinking())
                .call()
                .entity(
                        ReservationSelection.class,
                        spec -> spec.useProviderStructuredOutput()
                );
    }

    private String candidateContext(List<MyReservationSummary> reservations) {
        StringBuilder context = new StringBuilder();

        for (int index = 0; index < reservations.size(); index++) {
            MyReservationSummary reservation = reservations.get(index);
            context.append("""
                    후보 번호: %d
                    공연명: %s
                    공연 일시: %s
                    예매 상태: %s
                    예매 일시: %s

                    """.formatted(
                    index + 1,
                    reservation.eventTitle(),
                    reservation.performanceAt(),
                    reservation.reservationStatus(),
                    reservation.reservedAt()
            ));
        }

        return context.toString();
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^0-9a-z가-힣]", "");
    }
}
