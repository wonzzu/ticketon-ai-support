package com.ticketon.ai.client;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.refund.RefundSnapshot;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.reservation.dto.TicketOnPage;
import com.ticketon.ai.reservation.dto.TicketOnReservation;
import com.ticketon.ai.tool.result.ToolFailureCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Component
public class TicketOnClient {

    private final RestClient restClient;

    public TicketOnClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.ticketon.base-url}") String baseUrl,
            @Value("${app.ticketon.connect-timeout}") Duration connectTimeout,
            @Value("${app.ticketon.read-timeout}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            throw new TicketOnClientException(
                                    toFailureCode(response.getStatusCode())
                            );
                        }
                )
                .build();
    }

    public RefundSnapshot getRefundSnapshot(
            Long reservationId,
            TicketOnAccessToken accessToken
    ) {
        return execute(() -> {
            TicketOnResponse<RefundSnapshot> response = restClient.get()
                    .uri(
                            "/support/reservations/{reservationId}/refund-snapshot",
                            reservationId
                    )
                    .headers(headers -> headers.setBearerAuth(accessToken.value()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            return requireData(response);
        });
    }

    public List<MyReservationSummary> getMyReservations(TicketOnAccessToken accessToken) {
        return execute(() -> {
            TicketOnResponse<TicketOnPage<TicketOnReservation>> response =
                    restClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/reservations/me")
                                    .queryParam("size", 20)
                                    .build())
                            .headers(headers -> headers.setBearerAuth(accessToken.value()))
                            .retrieve()
                            .body(new ParameterizedTypeReference<>() {
                            });

            TicketOnPage<TicketOnReservation> page = requireData(response);

            return page.content().stream()
                    .map(MyReservationSummary::from)
                    .toList();
        });
    }

    private <T> T requireData(TicketOnResponse<T> response) {
        if (response == null || !response.success() || response.data() == null) {
            throw new TicketOnClientException(
                    ToolFailureCode.INVALID_RESPONSE
            );
        }

        return response.data();
    }

    private <T> T execute(Supplier<T> request) {
        try {
            return request.get();
        } catch (TicketOnClientException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw new TicketOnClientException(
                    toConnectionFailureCode(exception),
                    exception
            );
        } catch (RestClientException exception) {
            throw new TicketOnClientException(
                    ToolFailureCode.INVALID_RESPONSE,
                    exception
            );
        }
    }

    private static ToolFailureCode toFailureCode(HttpStatusCode status) {
        return switch (status.value()) {
            case 401 -> ToolFailureCode.AUTH_REQUIRED;
            case 403 -> ToolFailureCode.FORBIDDEN;
            case 404 -> ToolFailureCode.NOT_FOUND;
            default -> ToolFailureCode.UPSTREAM_UNAVAILABLE;
        };
    }

    private ToolFailureCode toConnectionFailureCode(Throwable exception) {
        if (hasCause(exception, HttpTimeoutException.class)
                || hasCause(exception, SocketTimeoutException.class)) {
            return ToolFailureCode.TIMEOUT;
        }

        return ToolFailureCode.UPSTREAM_UNAVAILABLE;
    }

    private boolean hasCause(
            Throwable exception,
            Class<? extends Throwable> causeType
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if (causeType.isInstance(cause)) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}
