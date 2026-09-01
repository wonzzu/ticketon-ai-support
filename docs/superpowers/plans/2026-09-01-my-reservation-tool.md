# My Reservation Tool Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 사용자의 TicketOn JWT를 LLM에 노출하지 않고 Spring AI `ToolContext`로 전달해 로그인 사용자의 예매 목록을 조회하는 read-only Tool을 만든다.

**Architecture:** `TicketOnClient`가 기존 `GET /reservations/me`를 Token Relay로 호출하고, `MyReservationTool`은 `ToolContext`에서만 토큰을 꺼낸다. `MyReservationToolCallingService`는 qwen3:8b에 Tool과 Tool Context를 요청 단위로 제공하며 기존 RAG 답변 흐름은 변경하지 않는다.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring AI 2.0.1, Spring `RestClient`, Ollama qwen3:8b, JUnit 5, AssertJ, Mockito

**Spec:** `docs/superpowers/specs/2026-09-01-support-tool-routing-design.md`

## Global Constraints

- JWT는 Tool 스키마, 사용자 프롬프트, Tool 결과, 최종 답변, 로그에 포함하지 않는다.
- AI Support는 JWT의 subject나 role을 해석하지 않고 TicketOn으로 그대로 전달한다.
- 개인 데이터 Tool은 read-only API만 호출한다.
- TicketOn이 JWT 검증과 예매 소유권 검증의 최종 책임을 가진다.
- 이번 계획은 기존 RAG 답변 Service와 Controller를 변경하지 않는다.
- 사용자의 명시적 요청 없이 commit, push, branch 작업을 하지 않는다.
- 테스트 실행은 사용자가 요청했을 때만 수행한다. 구현 시에는 테스트 코드를 먼저 작성하되 실행 승인을 별도로 받는다.

## File Structure

```text
src/main/java/com/ticketon/ai/
├─ client/
│  └─ TicketOnClient.java                         # TicketOn HTTP 호출
└─ reservation/
   ├─ dto/
   │  ├─ MyReservationSummary.java                # LLM에 전달할 최소 예매 정보
   │  ├─ TicketOnPage.java                        # TicketOn Page 응답의 content
   │  └─ TicketOnReservation.java                 # TicketOn 예매 응답 역직렬화
   ├─ service/
   │  └─ MyReservationToolCallingService.java     # ChatClient Tool Calling 요청
   └─ tool/
      └─ MyReservationTool.java                   # ToolContext 인증 및 목록 조회

src/test/java/com/ticketon/ai/
├─ client/TicketOnClientTest.java
└─ reservation/tool/MyReservationToolTest.java
```

---

### Task 1: TicketOn 내 예매 목록 Client

**Files:**
- Create: `src/main/java/com/ticketon/ai/reservation/dto/TicketOnPage.java`
- Create: `src/main/java/com/ticketon/ai/reservation/dto/TicketOnReservation.java`
- Create: `src/main/java/com/ticketon/ai/reservation/dto/MyReservationSummary.java`
- Modify: `src/main/java/com/ticketon/ai/client/TicketOnClient.java`
- Test: `src/test/java/com/ticketon/ai/client/TicketOnClientTest.java`

**Interfaces:**
- Consumes: `TicketOnAccessToken(String value)`, TicketOn `GET /reservations/me`
- Produces: `List<MyReservationSummary> TicketOnClient.getMyReservations(TicketOnAccessToken)`

- [ ] **Step 1: TicketOnClient의 Token Relay 테스트를 먼저 작성한다**

```java
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
```

- [ ] **Step 2: 사용자가 요청하면 테스트를 실행해 기능 부재로 실패하는지 확인한다**

Run:

```powershell
.\gradlew.bat test --tests "com.ticketon.ai.client.TicketOnClientTest"
```

Expected: `getMyReservations`와 예매 DTO가 아직 없어 컴파일 실패한다.

- [ ] **Step 3: 최소 응답 DTO를 구현한다**

```java
package com.ticketon.ai.reservation.dto;

import tools.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketOnPage<T>(List<T> content) {
}
```

```java
package com.ticketon.ai.reservation.dto;

import tools.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketOnReservation(
        Long id,
        String eventTitle,
        LocalDateTime showDateTime,
        String status,
        LocalDateTime createdAt
) {
}
```

```java
package com.ticketon.ai.reservation.dto;

import java.time.LocalDateTime;

public record MyReservationSummary(
        Long reservationId,
        String eventTitle,
        LocalDateTime performanceAt,
        String reservationStatus,
        LocalDateTime reservedAt
) {

    public static MyReservationSummary from(TicketOnReservation reservation) {
        return new MyReservationSummary(
                reservation.id(),
                reservation.eventTitle(),
                reservation.showDateTime(),
                reservation.status(),
                reservation.createdAt()
        );
    }
}
```

- [ ] **Step 4: TicketOnClient에 내 예매 조회를 추가한다**

`TicketOnClient`에 다음 메서드를 추가한다.

```java
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
        throw new IllegalStateException("TicketOn 내 예매 목록 응답이 올바르지 않습니다.");
    }

    return response.data().content().stream()
            .map(MyReservationSummary::from)
            .toList();
}
```

- [ ] **Step 5: 사용자가 요청하면 Client 테스트와 기존 테스트를 실행한다**

Run:

```powershell
.\gradlew.bat test --tests "com.ticketon.ai.client.TicketOnClientTest"
.\gradlew.bat test
```

Expected: 모든 테스트가 통과하고 Authorization 값은 테스트 출력과 애플리케이션 로그에 기록되지 않는다.

---

### Task 2: JWT를 LLM에 노출하지 않는 MyReservationTool

**Files:**
- Create: `src/main/java/com/ticketon/ai/reservation/tool/MyReservationTool.java`
- Test: `src/test/java/com/ticketon/ai/reservation/tool/MyReservationToolTest.java`

**Interfaces:**
- Consumes: `ToolContext`, `TicketOnClient.getMyReservations(TicketOnAccessToken)`
- Produces: `List<MyReservationSummary> MyReservationTool.getMyReservations(ToolContext)`

- [ ] **Step 1: ToolContext 인증 경계 테스트를 먼저 작성한다**

```java
package com.ticketon.ai.reservation.tool;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.client.TicketOnClient;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MyReservationToolTest {

    private final TicketOnClient ticketOnClient = mock(TicketOnClient.class);
    private final MyReservationTool tool = new MyReservationTool(ticketOnClient);

    @Test
    void ToolContext의_JWT로_본인_예매만_조회한다() {
        TicketOnAccessToken token = new TicketOnAccessToken("user-token");
        ToolContext context = new ToolContext(Map.of(
                MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY,
                token
        ));
        List<MyReservationSummary> reservations = List.of();
        when(ticketOnClient.getMyReservations(token)).thenReturn(reservations);

        assertThat(tool.getMyReservations(context)).isSameAs(reservations);
        verify(ticketOnClient).getMyReservations(token);
    }

    @Test
    void JWT가_없으면_TicketOn을_호출하지_않는다() {
        ToolContext context = new ToolContext(Map.of());

        assertThatThrownBy(() -> tool.getMyReservations(context))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
        verify(ticketOnClient, never()).getMyReservations(org.mockito.ArgumentMatchers.any());
    }
}
```

- [ ] **Step 2: 사용자가 요청하면 테스트를 실행해 MyReservationTool 부재로 실패하는지 확인한다**

Run:

```powershell
.\gradlew.bat test --tests "com.ticketon.ai.reservation.tool.MyReservationToolTest"
```

Expected: `MyReservationTool`이 없어 컴파일 실패한다.

- [ ] **Step 3: read-only Tool을 구현한다**

```java
package com.ticketon.ai.reservation.tool;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.client.TicketOnClient;
import com.ticketon.ai.reservation.dto.MyReservationSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MyReservationTool {

    public static final String ACCESS_TOKEN_CONTEXT_KEY = "ticketOnAccessToken";

    private final TicketOnClient ticketOnClient;

    @Tool(description = "로그인한 사용자의 TicketOn 예매 목록을 조회합니다. 사용자가 자신의 실제 예매, 공연 일정 또는 예매 상태를 묻는 경우에만 사용합니다.")
    public List<MyReservationSummary> getMyReservations(ToolContext toolContext) {
        Object value = toolContext.getContext().get(ACCESS_TOKEN_CONTEXT_KEY);

        if (!(value instanceof TicketOnAccessToken accessToken)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "TicketOn 로그인이 필요합니다."
            );
        }

        return ticketOnClient.getMyReservations(accessToken);
    }
}
```

- [ ] **Step 4: 사용자가 요청하면 Tool 테스트를 실행한다**

Run:

```powershell
.\gradlew.bat test --tests "com.ticketon.ai.reservation.tool.MyReservationToolTest"
```

Expected: 두 테스트가 통과하며 JWT가 Tool의 모델 입력 파라미터에 존재하지 않는다.

---

### Task 3: Spring AI Tool Calling 요청 연결

**Files:**
- Create: `src/main/java/com/ticketon/ai/reservation/service/MyReservationToolCallingService.java`

**Interfaces:**
- Consumes: 사용자 질문, `TicketOnAccessToken`, `MyReservationTool`
- Produces: `String MyReservationToolCallingService.answer(String, TicketOnAccessToken)`

- [ ] **Step 1: Tool Calling Service를 구현하기 전에 호출 계약을 검토한다**

호출 계약은 다음으로 고정한다.

```java
String answer(String question, TicketOnAccessToken accessToken)
```

토큰은 사용자 프롬프트 문자열에 삽입하지 않고 `toolContext`에만 넣는다.

- [ ] **Step 2: 최소 Tool Calling Service를 구현한다**

```java
package com.ticketon.ai.reservation.service;

import com.ticketon.ai.auth.TicketOnAccessToken;
import com.ticketon.ai.reservation.tool.MyReservationTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MyReservationToolCallingService {

    private static final String SYSTEM_PROMPT = """
            당신은 TicketOn 고객지원 상담원입니다.

            사용자가 자신의 실제 예매 목록, 예매 상태 또는 공연 일정을 물으면
            반드시 getMyReservations Tool을 사용하세요.

            Tool 결과에 없는 정보는 추측하지 마세요.
            여러 예매 중 사용자가 말한 대상을 확정할 수 없으면 임의로 선택하지 말고
            어떤 공연인지 다시 질문하세요.
            실제 예매 취소나 상태 변경을 실행할 수 있다고 말하지 마세요.
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final MyReservationTool myReservationTool;

    public String answer(String question, TicketOnAccessToken accessToken) {
        return chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .tools(myReservationTool)
                .toolContext(Map.of(
                        MyReservationTool.ACCESS_TOKEN_CONTEXT_KEY,
                        accessToken
                ))
                .options(OllamaChatOptions.builder().disableThinking())
                .call()
                .content();
    }
}
```

- [ ] **Step 3: 컴파일 또는 실행은 사용자가 요청할 때 수행한다**

Run:

```powershell
.\gradlew.bat compileJava
```

Expected: Spring AI 2.0.1의 `.tools(...)`, `.toolContext(...)` API로 컴파일된다.

- [ ] **Step 4: Ollama와 TicketOn이 준비됐을 때 실제 Tool 선택을 수동 확인한다**

검증 질문:

```text
내가 예매한 공연 목록 보여줘.
```

Expected:

```text
qwen3:8b가 getMyReservations를 선택
→ JWT는 ToolContext에만 존재
→ TicketOn GET /reservations/me 호출
→ 본인 예매 목록을 자연어로 설명
```

## Completion Gate

- `TicketOnClientTest`와 `MyReservationToolTest`가 통과한다.
- 전체 테스트가 통과한다.
- 실제 Tool Calling에서 TicketOn이 받은 JWT로 사용자 인증에 성공한다.
- 모델 입력과 로그에 JWT가 없음을 확인한다.
- 기존 `PolicyAnswerService`와 Retrieval 평가 결과가 변경되지 않는다.
