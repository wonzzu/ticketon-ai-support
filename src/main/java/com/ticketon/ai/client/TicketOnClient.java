package com.ticketon.ai.client;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.refund.RefundSnapshot;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.reservation.dto.TicketOnPage;
import com.ticketon.ai.reservation.dto.TicketOnReservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class TicketOnClient {

    private final RestClient restClient;

    public TicketOnClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.ticketon.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public RefundSnapshot getRefundSnapshot(
            Long reservationId,
            TicketOnAccessToken accessToken
    ) {
        TicketOnResponse<RefundSnapshot> response = restClient.get()
                .uri(
                        "/support/reservations/{reservationId}/refund-snapshot",
                        reservationId
                )
                .headers(headers -> headers.setBearerAuth(accessToken.value()))
                .retrieve()
                .onStatus(
                        status -> status.value() == 401,
                        (request, responses) -> {
                            throw new ResponseStatusException(
                                    HttpStatusCode.valueOf(401),
                                    "TicketOn 로그인이 만료되었거나 유효하지 않습니다."
                            );
                        }
                )
                .onStatus(
                        status -> status.value() == 403,
                        (request, responses) -> {
                            throw new ResponseStatusException(
                                    HttpStatusCode.valueOf(403),
                                    "본인의 예매만 조회할 수 있습니다."
                            );
                        }
                )
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalStateException(
                    "TicketOn 환불 스냅샷 응답이 올바르지 않습니다."
            );
        }

        return response.data();
    }

    public List<MyReservationSummary> getMyReservations(TicketOnAccessToken accessToken) {
        TicketOnResponse<TicketOnPage<TicketOnReservation>> response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/reservations/me")
                        .queryParam("size", 20)
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken.value()))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalStateException(
                    "TicketOn 내 예매 목록 응답이 올바르지 않습니다."
            );
        }

        return response.data().content().stream()
                .map(MyReservationSummary::from)
                .toList();
    }
}
