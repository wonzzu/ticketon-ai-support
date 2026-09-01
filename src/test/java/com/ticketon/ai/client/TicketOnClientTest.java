package com.ticketon.ai.client;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TicketOnClientTest {

    @Test
    void 사용자의_JWT로_내_예매_목록을_조회한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TicketOnClient client = new TicketOnClient(builder, "http://ticketon.test");

        server.expect(once(), requestTo("http://ticketon.test/reservations/me?size=20"))
                .andExpect(method(GET))
                .andExpect(header(AUTHORIZATION, "Bearer user-token"))
                .andRespond(withSuccess("""
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
                        """, APPLICATION_JSON));

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
        server.verify();
    }
}
