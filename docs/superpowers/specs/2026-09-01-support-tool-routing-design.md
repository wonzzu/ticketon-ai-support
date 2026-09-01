# TicketOn AI Support Tool Routing 설계

## 1. 목적

TicketOn AI Support가 일반 정책 질문과 사용자별 데이터가 필요한 질문을 하나의 진입점에서 처리한다.

LLM은 질문에 필요한 작업을 선택하지만 인증, 권한, 소유권, 금액과 시간 계산은 결정하지 않는다. 보안 검증은 AI Support의 Java 코드와 TicketOn 백엔드가 담당한다.

## 2. 핵심 원칙

- 별도의 Router LLM을 추가하지 않고 Spring AI Tool Calling의 Tool 선택을 라우팅으로 사용한다.
- 정책 검색은 인증 없이 사용할 수 있는 read-only Tool로 제공한다.
- 개인 데이터 Tool은 사용자가 전달한 TicketOn JWT가 있어야 호출할 수 있다.
- JWT는 `ToolContext`를 통해 Java Tool에만 전달하고 LLM 입력이나 Tool 파라미터에 포함하지 않는다.
- AI Support는 JWT를 해석하거나 회원 ID를 신뢰하지 않고 TicketOn API로 그대로 전달한다.
- TicketOn은 JWT를 검증하고 인증된 회원의 데이터인지 소유권을 다시 검사한다.
- 상태 변경 Tool은 이번 범위에 포함하지 않는다.
- 날짜, 시간, 수수료, 환불액은 Java가 계산하고 LLM은 계산 결과만 설명한다.

## 3. 전체 흐름

```text
사용자 질문 + 선택적 Authorization 헤더
        ↓
SupportAnswerController
        ↓
SupportAnswerService
        ↓
Spring AI Tool Calling
        ├─ PolicySearchTool
        ├─ MyReservationTool
        └─ RefundEstimateTool
        ↓
Java Tool 보안 검증
        ↓
TicketOn read-only API 또는 기존 RAG 실행
        ↓
Java 결정 로직
        ↓
LLM이 조회·검색·계산 결과를 자연어로 설명
```

## 4. 구성 요소

### 4.1 SupportAnswerController

- 사용자 질문을 받는다.
- `Authorization` 헤더는 선택적으로 받는다.
- JWT 원문을 로그에 남기지 않는다.
- 질문과 안전하게 변환한 `TicketOnAccessToken`을 Service에 전달한다.

정책 질문은 로그인하지 않은 사용자도 사용할 수 있다. 개인 데이터 Tool이 필요하지만 JWT가 없다면 로그인이 필요하다는 응답을 반환한다.

### 4.2 SupportAnswerService

- Tool을 요청 단위로 ChatClient에 제공한다.
- JWT가 있으면 `ToolContext`에 넣는다.
- Tool 실행 결과를 받은 LLM의 최종 답변을 반환한다.
- 별도의 Router LLM 호출을 수행하지 않는다.

### 4.3 PolicySearchTool

- 인증이 필요하지 않다.
- 기존 `PolicyRetrievalService`를 그대로 호출한다.
- 최종 Retrieval 파이프라인인 Query Rewrite, Vector Top-10, policyId Dedup, Reranking, Top-3를 재사용한다.
- 검색된 정책의 ID, 제목, 내용을 LLM에 반환한다.

### 4.4 MyReservationTool

- JWT가 필요한 read-only Tool이다.
- LLM이 보는 Tool 입력에는 JWT와 memberId가 없다.
- `ToolContext`에서 `TicketOnAccessToken`을 꺼낸다.
- TicketOn의 내 예매 목록 API에 JWT를 전달한다.
- 공연명, 공연 시각, 예매 상태, 예매 시각 등 대상 식별에 필요한 최소 데이터만 반환한다.
- 질문과 일치하는 예매가 여러 개이면 임의로 하나를 선택하지 않고 사용자에게 구분을 요청한다.

### 4.5 RefundEstimateTool

- 첫 Tool Calling 연결 이후 추가한다.
- 선택된 본인 예매의 환불 스냅샷을 TicketOn에서 조회한다.
- TicketOn이 JWT 검증과 예매 소유권 검사를 수행한다.
- Java 계산기가 취소 가능 여부, 무료 취소 예외, 수수료, 환불 예정액을 계산한다.
- LLM에는 원본 JWT나 개인정보 대신 계산 결과와 적용 정책 ID만 전달한다.

## 5. Tool 선택과 보안의 분리

LLM이 결정할 수 있는 것은 필요한 Tool의 종류뿐이다.

```text
정책 질문           → PolicySearchTool
내 예매 질문        → MyReservationTool
내 환불 예상 질문   → PolicySearchTool + RefundEstimateTool
```

다음 판단은 항상 Java와 TicketOn이 담당한다.

- 인증 여부
- Tool 사용 허용 여부
- 예매 소유권
- 상태 변경 가능 여부
- 날짜와 시간의 경계조건
- 수수료와 환불액

LLM이 잘못된 Tool을 선택해도 권한 검사를 우회할 수 없어야 한다.

## 6. 오류 처리

- JWT 없음: 개인 Tool 실행 전에 로그인 필요 응답
- JWT 만료 또는 위조: TicketOn의 401을 인증 실패로 변환
- 타인 예매: TicketOn의 403을 권한 실패로 변환
- 예매 없음: TicketOn의 404를 조회 실패로 변환
- TicketOn Timeout 또는 5xx: 정책 답변과 구분되는 외부 시스템 장애 응답
- Tool 결과 형식 오류: 추측하지 않고 현재 정보를 확인할 수 없다고 응답

오류 메시지와 로그에 JWT, Authorization 헤더, 개인정보를 포함하지 않는다.

## 7. 구현 순서

1. `getMyReservations`용 TicketOn Client 응답 DTO와 조회 메서드
2. `MyReservationTool`과 JWT `ToolContext` 연결
3. Tool 선택과 Token Relay 핵심 테스트
4. `PolicySearchTool`로 기존 RAG 노출
5. 단일 `SupportAnswerService`에서 두 Tool 제공
6. `SupportAnswerController` 연결
7. Tool 선택 평가셋으로 정책 질문과 개인 질문 분류 검증
8. `RefundEstimateTool`과 Java 환불 계산기 추가
9. Tool 실패 및 권한 공격 검증

## 8. 검증 기준

- 정책 질문은 JWT 없이 PolicySearchTool만 사용한다.
- 개인 예매 질문은 MyReservationTool을 사용한다.
- JWT가 Tool 스키마, LLM 프롬프트, 답변, 로그에 노출되지 않는다.
- JWT가 없으면 TicketOn API를 호출하지 않는다.
- 타인 예매 조회는 TicketOn 소유권 검사에서 차단된다.
- LLM이 허용되지 않은 상태 변경 작업을 실행할 수 없다.
- 날짜와 금액 계산 결과는 LLM이 아니라 Java 결과와 일치한다.

## 9. 이번 첫 구현 범위

첫 구현은 `MyReservationTool`의 세로 흐름까지만 다룬다.

```text
질문 + JWT
→ ChatClient Tool 선택
→ ToolContext의 JWT
→ TicketOn 내 예매 목록 API
→ 최소 예매 목록
→ LLM 답변
```

환불 계산과 실제 취소 실행은 포함하지 않는다.
