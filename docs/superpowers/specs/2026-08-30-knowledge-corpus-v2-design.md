# TicketOn AI Support 지식 Corpus·Retrieval 평가 V2 설계

## 1. 목적

TicketOn 실제 코드가 제공하는 일반 회원, 판매자, 관리자 기능을 근거로 정적 고객지원 지식을 확장한다.
특정 목표 숫자를 채우지 않고, 사용자에게 설명할 가치가 있는 독립적인 지식 단위를 추출한 결과로 최종 Chunk 수를 결정한다.

V2는 다음을 검증할 수 있어야 한다.

- 유사한 정책·FAQ·가이드가 함께 존재할 때 정답 근거가 Top-3에 포함되는가
- 구어체, 생략형, 경계조건, 잘못된 전제에서도 검색되는가
- 기존 24개 정책과 65개 Development 문항의 동작이 유지되는가
- Query Rewrite와 Reranker의 효과를 같은 Development 평가셋으로 비교할 수 있는가

## 2. 근거와 범위

지식은 다음 저장소를 근거로 작성한다.

- `ticketing`: Controller, Service, Domain, DTO Validation, BaseResponseStatus, SecurityConfig
- `ticketing-docs`: 인증, 회원, 공연, 좌석, 대기열, 예매, 결제, 쿠폰, 리뷰, 정산 설명
- `ticketon-ai-support/policies`: 기존 정책 24개

포함 대상:

- 일반 회원의 가입, 로그인, 회원 상태와 프로필
- 공연 검색, 상세, 회차, 공연장, 좌석 등급과 가격
- 대기열, 좌석 선택·선점, 예매, 결제와 취소
- 쿠폰 발급, 리뷰
- 판매자 가입, 공연 등록·검수, 회차·가격, 포스터, 정산
- 관리자 권한이 필요한 회원·공연·공연장 관리의 이용 안내
- 요청 제한, 권한, 입력 오류 등 사용자가 조치할 수 있는 공통 오류

제외 대상:

- Outbox Relay, Redis Lua, 분산락, 배치 내부 구현처럼 사용자가 알 필요가 없는 내부 기술 설명
- 개인의 현재 예매·결제·쿠폰·정산 상태처럼 Tool Calling이 필요한 동적 데이터
- 실제 TicketOn 코드나 확정 정책에 근거가 없는 기능
- 성능 부하용 가상 벡터

## 3. 구현 상태 분류

각 Chunk는 다음 `implementationStatus` 중 하나를 가진다.

- `IMPLEMENTED`: TicketOn 코드로 동작이 확인됨
- `PLANNED`: 정책은 확정됐지만 TicketOn 구현 전

기능 사용법이나 오류 대응 안내 여부는 `implementationStatus`가 아니라
`documentType`의 `GUIDE` 또는 `ERROR_GUIDE`로 구분한다.

`PLANNED` 문서는 검색 대상에는 포함할 수 있지만 본문에서 현재 구현된 기능처럼 표현하지 않는다.

현재 기존 환불 정책 중 날짜별 수수료, 24시간 예외, 카드 반영 기간, 공연 변경 무수수료 취소는 TicketOn 구현과 별도로 `PLANNED` 여부를 명시해야 한다.

## 4. Chunk 모델

기존 `policyId` 하나로 문서와 원천 정책을 동시에 표현하지 않는다.

```text
chunkId              검색 문서의 고유 ID
sourcePolicyId        원천 정책 ID. 직접 정책이면 chunkId와 같음
documentType          POLICY | FAQ | GUIDE | NOTICE | ERROR_GUIDE
audience              CUSTOMER | SELLER | ADMIN | COMMON
domain                AUTH | MEMBER | EVENT | VENUE | QUEUE | SEAT | RESERVATION |
                      PAYMENT | REFUND | COUPON | REVIEW | SETTLEMENT | COMMON
implementationStatus  IMPLEMENTED | PLANNED
version
effectiveFrom
status                ACTIVE | INACTIVE
title
content
```

한 Chunk는 하나의 주된 사용자 판단만 설명한다. 여러 정책이 필요한 내용은 `sourcePolicyIds` 목록으로 확장하지 않고, 우선 주된 `sourcePolicyId` 하나를 선택한다. 실제 복합 근거 평가는 평가 문항의 `expectedPolicyIds`에서 표현한다.

UUID는 `chunkId`로 생성하여 같은 정책에서 파생된 FAQ들이 서로 덮어쓰이지 않게 한다.

검색 응답은 `chunkId`, `sourcePolicyId`, `documentType`, `title`, `content`, `similarityScore`를 제공한다.
평가기는 검색된 Chunk의 `sourcePolicyId`를 기준으로 Recall@3를 계산한다.

## 5. Markdown 형식

핵심 정책 문서는 기존 형식을 유지하고 누락 Metadata에 기본값을 적용한다.

지원 문서는 파일 Front Matter에 공통값을 두고, 각 `##` Chunk 아래에 원천 정책을 표시한다.

```markdown
---
domain: QUEUE
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
version: "1.0"
effectiveFrom: "2026-09-01"
status: ACTIVE
---

## FAQ-QUEUE-01 친구와 동시에 눌렀는데 순번이 다른 이유
sourcePolicyId: QUEUE-02

대기 순서는 ...
```

Parser는 `sourcePolicyId:`를 본문에서 제거하고 Metadata로 저장한다.

## 6. Corpus 산출 방식

목표 숫자를 먼저 고정하지 않는다. 다음 절차로 목록을 만든다.

1. TicketOn 사용자 행동과 오류코드를 독립적인 지식 주제로 추출
2. 같은 판단을 반복하는 항목 통합
3. 정책, FAQ, 가이드로 구분할 가치가 있을 때만 별도 Chunk 생성
4. 각 Chunk에 근거 코드 또는 정책 ID 연결
5. 동적 데이터 질문과 내부 기술 설명 제거

현재 코드 조사로 예상되는 최종 범위는 약 150~180 Chunk다. 최종 개수는 Manifest 검수 후 확정한다.

Manifest는 다음 열을 가진다.

```text
chunkId | title | domain | documentType | audience |
sourcePolicyId | implementationStatus | evidence
```

## 7. 평가 V2

Retrieval 평가에는 정적 문서로 답할 수 있는 질문만 포함한다.

```text
Clean Regression        기존 41문항
Realistic Regression    기존 24문항
Expanded Development    신규 영역 약 55~75문항
Holdout V2              약 35~45문항
```

최종 Retrieval 문항 수는 약 155~185개를 예상하되, Chunk와 1:1로 맞추지 않는다.

질문은 Intent와 난이도를 기준으로 분배한다.

- `DIRECT`
- `PARAPHRASE`
- `COLLOQUIAL`
- `IMPLICIT`
- `NOISY`
- `BOUNDARY`
- `WRONG_ASSUMPTION`
- `POLICY_CONFUSION`
- `MULTI_INTENT`

`ABSTENTION`과 `TOOL_REQUIRED`는 Retrieval Recall@3에 섞지 않고 후속 단계의 별도 평가셋으로 관리한다.

Holdout V2는 Corpus와 평가 기준을 확정한 후 작성하며, 최초 실행 전까지 검색 개선에 사용하지 않는다.

## 8. 비교 실험

Corpus V2 적재 후 다음 순서로 측정한다.

1. 기본 Vector Search로 Clean Regression 측정
2. 기본 Vector Search로 Realistic Regression 측정
3. 기본 Vector Search로 Expanded Development 측정
4. 실패 문항 Top-5와 유사도 분석
5. 수동 Query Rewrite 진단
6. LLM Query Rewrite를 전체 Development에 적용
7. 필요할 때만 Reranker 실험
8. Retrieval 설정 동결
9. Holdout V2 최초 실행

비교 시 전체 Recall@3뿐 아니라 데이터셋, 난이도, 도메인별 Recall@3와 기존 성공 문항의 Regression을 함께 기록한다.

## 9. 안전성과 실패 처리

- 문서에 비밀번호, 토큰, 개인 데이터, 로컬 접속 정보를 넣지 않는다.
- 개인 상태를 정적 문서로 추측하지 않는다.
- 원천 정책이 없는 FAQ는 작성하지 않는다.
- `PLANNED`와 `IMPLEMENTED`를 혼동하지 않는다.
- 같은 `chunkId`와 상충하는 Active 문서를 적재하지 않는다.
- Parser 오류, 필수 Metadata 누락, 중복 ID는 적재 전에 실패시킨다.
- 재적재 시 기존 문서가 남아 Corpus가 섞이지 않도록 버전 또는 명시적인 교체 전략을 사용한다.

## 10. 구현 단계

1. Chunk Manifest 작성 및 검수
2. Chunk 모델과 Parser Metadata 확장
3. Parser·Ingestion 단위 테스트
4. 지원 Markdown을 도메인별 작은 묶음으로 작성
5. Expanded Development 작성
6. Holdout V2 작성 후 봉인
7. 전체 Corpus 재적재 및 개수·중복·차원 검증
8. Baseline 재측정과 실패 분석

각 단계는 별도로 검증하며, 문서 작성과 검색 개선을 한 번에 섞지 않는다.
