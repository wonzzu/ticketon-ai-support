package com.ticketon.ai.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import com.ticketon.ai.tool.result.ToolFailureCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketOnClientTest {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(1);
    private static final Duration SHORT_READ_TIMEOUT = Duration.ofMillis(100);

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void 사용자의_JWT로_내_예매_목록을_조회한다() throws IOException {
        String baseUrl = startServer(200, successResponse(), Duration.ZERO);
        TicketOnClient client = createClient(baseUrl);

        List<MyReservationSummary> result = client.getMyReservations(
                new TicketOnAccessToken("user-token")
        );

        assertThat(result).containsExactly(new MyReservationSummary(
                10L,
                "TicketOn 콘서트",
                LocalDateTime.of(2026, 9, 5, 18, 0),
                "CONFIRMED",
                LocalDateTime.of(2026, 9, 1, 10, 0)
        ));
    }

    @Test
    void TicketOn_인증실패를_안전한_실패코드로_변환한다() throws IOException {
        String baseUrl = startServer(401, "", Duration.ZERO);
        TicketOnClient client = createClient(baseUrl);

        assertFailureCode(
                () -> client.getMyReservations(
                        new TicketOnAccessToken("invalid-token")
                ),
                ToolFailureCode.AUTH_REQUIRED
        );
    }

    @Test
    void TicketOn_서버오류를_외부서비스_장애로_변환한다() throws IOException {
        String baseUrl = startServer(500, "", Duration.ZERO);
        TicketOnClient client = createClient(baseUrl);

        assertFailureCode(
                () -> client.getMyReservations(
                        new TicketOnAccessToken("user-token")
                ),
                ToolFailureCode.UPSTREAM_UNAVAILABLE
        );
    }

    @Test
    void TicketOn_응답이_늦으면_TIMEOUT으로_변환한다() throws IOException {
        String baseUrl = startServer(
                200,
                successResponse(),
                Duration.ofMillis(500)
        );
        TicketOnClient client = createClient(baseUrl, SHORT_READ_TIMEOUT);

        assertFailureCode(
                () -> client.getMyReservations(
                        new TicketOnAccessToken("user-token")
                ),
                ToolFailureCode.TIMEOUT
        );
    }

    @Test
    void TicketOn에_연결할_수_없으면_UPSTREAM_UNAVAILABLE로_변환한다()
            throws IOException {
        int closedPort;

        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }

        TicketOnClient client = createClient(
                "http://127.0.0.1:" + closedPort
        );

        assertFailureCode(
                () -> client.getMyReservations(
                        new TicketOnAccessToken("user-token")
                ),
                ToolFailureCode.UPSTREAM_UNAVAILABLE
        );
    }

    private TicketOnClient createClient(String baseUrl) {
        return createClient(baseUrl, READ_TIMEOUT);
    }

    private TicketOnClient createClient(
            String baseUrl,
            Duration readTimeout
    ) {
        return new TicketOnClient(
                RestClient.builder(),
                baseUrl,
                CONNECT_TIMEOUT,
                readTimeout
        );
    }

    private String startServer(
            int status,
            String responseBody,
            Duration delay
    ) throws IOException {
        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );
        server.createContext(
                "/reservations/me",
                exchange -> respond(exchange, status, responseBody, delay)
        );
        server.start();

        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private void respond(
            HttpExchange exchange,
            int status,
            String responseBody,
            Duration delay
    ) throws IOException {
        waitFor(delay);

        byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json"
        );
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private void waitFor(Duration delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void assertFailureCode(
            Runnable request,
            ToolFailureCode expectedCode
    ) {
        assertThatThrownBy(request::run)
                .isInstanceOfSatisfying(
                        TicketOnClientException.class,
                        exception -> {
                            assertThat(exception.getFailureCode())
                                    .isEqualTo(expectedCode);
                            assertThat(exception.getMessage())
                                    .isEqualTo(expectedCode.getSafeMessage());
                        }
                );
    }

    private String successResponse() {
        return """
                {
                  "success": true,
                  "code": 1000,
                  "message": "성공",
                  "data": {
                    "content": [
                      {
                        "id": 10,
                        "eventTitle": "TicketOn 콘서트",
                        "showDateTime": "2026-09-05T18:00:00",
                        "status": "CONFIRMED",
                        "createdAt": "2026-09-01T10:00:00"
                      }
                    ]
                  }
                }
                """;
    }
}
