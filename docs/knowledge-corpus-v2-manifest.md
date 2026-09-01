# TicketOn Knowledge Corpus V2 Manifest

이 문서는 TicketOn 실제 기능을 고객지원용 검색 Chunk로 확장하기 위한 목록이다.
숫자를 채우기 위한 가상 정책이 아니라 `ticketing` 코드, `ticketing-docs`, 기존 AI 정책을 근거로 작성한다.

## 작성 기준

- `POLICY`: 한 가지 규칙이나 사용 조건을 설명하는 원천 문서
- `FAQ`: 실제 사용자가 자주 묻는 표현으로 원천 정책을 설명하는 문서
- `GUIDE`: 여러 화면이나 처리 단계를 순서대로 안내하는 문서
- `ERROR_GUIDE`: 오류 원인과 사용자가 할 수 있는 조치를 설명하는 문서
- `IMPLEMENTED`: 현재 TicketOn 코드에서 확인된 동작
- `PLANNED`: AI 정책에는 있으나 TicketOn 코드에는 아직 구현되지 않은 동작
- 개인의 실제 예매·결제·쿠폰·정산 상태는 정적 문서가 아니라 Tool Calling 대상으로 제외

## AUTH — 인증·권한 (10)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| AUTH-01 | 로그인에 필요한 정보 | POLICY | COMMON | AUTH-01 | IMPLEMENTED | `LoginRequestDto`, `AuthService` |
| AUTH-02 | Access Token이 만료된 경우 | POLICY | COMMON | AUTH-02 | IMPLEMENTED | `AuthService.reissue` |
| AUTH-03 | Refresh Token으로 로그인 연장 | POLICY | COMMON | AUTH-03 | IMPLEMENTED | `AuthController.refresh` |
| AUTH-04 | 로그아웃 후 토큰 처리 | POLICY | COMMON | AUTH-04 | IMPLEMENTED | `AuthService.logout` |
| AUTH-05 | 로그인하지 않고 이용할 수 있는 기능 | POLICY | COMMON | AUTH-05 | IMPLEMENTED | `SecurityConfig` 공개 경로 |
| AUTH-06 | 일반 회원 전용 기능 | POLICY | CUSTOMER | AUTH-06 | IMPLEMENTED | Router role guard, SecurityConfig |
| AUTH-07 | 판매자 전용 기능 | POLICY | SELLER | AUTH-07 | IMPLEMENTED | `SecurityConfig` SELLER 경로 |
| AUTH-08 | 관리자 전용 기능 | POLICY | ADMIN | AUTH-08 | IMPLEMENTED | `SecurityConfig` ADMIN 경로 |
| AUTH-09 | 인증 필요 오류와 권한 없음 오류의 차이 | ERROR_GUIDE | COMMON | AUTH-09 | IMPLEMENTED | 2005, 2006 상태코드 |
| AUTH-10 | 요청을 너무 많이 보낸 경우 | ERROR_GUIDE | COMMON | AUTH-10 | IMPLEMENTED | `RateLimitAspect`, 9003 상태코드 |

## MEMBER — 회원 (10)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| MEMBER-01 | 일반 회원 가입 방법 | GUIDE | CUSTOMER | MEMBER-01 | IMPLEMENTED | `NormalMemberController.signup` |
| MEMBER-02 | 판매자 회원 가입 방법 | GUIDE | SELLER | MEMBER-02 | IMPLEMENTED | `SellerController.signup` |
| MEMBER-03 | 이미 사용 중인 이메일 | ERROR_GUIDE | COMMON | MEMBER-03 | IMPLEMENTED | 2001 상태코드 |
| MEMBER-04 | 이미 사용 중인 닉네임 | ERROR_GUIDE | CUSTOMER | MEMBER-04 | IMPLEMENTED | 2002 상태코드 |
| MEMBER-05 | 내 회원 정보 조회 | GUIDE | COMMON | MEMBER-05 | IMPLEMENTED | `MemberController`, 회원별 `/me` |
| MEMBER-06 | 일반 회원 정보 수정 범위 | POLICY | CUSTOMER | MEMBER-06 | IMPLEMENTED | `MemberUpdateDto` |
| MEMBER-07 | 판매자 정보 수정 범위 | POLICY | SELLER | MEMBER-07 | IMPLEMENTED | `SellerUpdateDto` |
| MEMBER-08 | 회원 탈퇴 처리 | POLICY | COMMON | MEMBER-08 | IMPLEMENTED | 회원별 DELETE `/me` |
| MEMBER-09 | 회원 상태 종류와 이용 제한 | POLICY | COMMON | MEMBER-09 | IMPLEMENTED | `MemberStatus` |
| MEMBER-10 | 정지 회원의 이용과 해제 | POLICY | COMMON | MEMBER-10 | IMPLEMENTED | `AdminMemberService` |

## EVENT — 공연 탐색·정보 (15)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| EVENT-01 | 공연 목록 조회 | GUIDE | COMMON | EVENT-01 | IMPLEMENTED | GET `/events` |
| EVENT-02 | 공연 제목으로 검색 | GUIDE | COMMON | EVENT-02 | IMPLEMENTED | `EventService.search` |
| EVENT-03 | 공연 카테고리로 검색 | GUIDE | COMMON | EVENT-03 | IMPLEMENTED | `Category` |
| EVENT-04 | 공연 상세 정보에 포함되는 항목 | POLICY | COMMON | EVENT-04 | IMPLEMENTED | `EventResponseDto` |
| EVENT-05 | 전체 관람가 공연 | POLICY | COMMON | EVENT-05 | IMPLEMENTED | `AgeLimit.ALL` |
| EVENT-06 | 12세 이상 관람가 | POLICY | COMMON | EVENT-06 | IMPLEMENTED | `AgeLimit.AGE_12` |
| EVENT-07 | 15세 이상 관람가 | POLICY | COMMON | EVENT-07 | IMPLEMENTED | `AgeLimit.AGE_15` |
| EVENT-08 | 18세 이상 관람가 | POLICY | COMMON | EVENT-08 | IMPLEMENTED | `AgeLimit.AGE_18` |
| EVENT-09 | 공연 검수 대기 상태 | POLICY | SELLER | EVENT-09 | IMPLEMENTED | `EventStatus.PENDING` |
| EVENT-10 | 게시 중인 공연 상태 | POLICY | COMMON | EVENT-10 | IMPLEMENTED | `EventStatus.APPROVED` |
| EVENT-11 | 반려된 공연 상태 | POLICY | SELLER | EVENT-11 | IMPLEMENTED | `EventStatus.REJECTED` |
| EVENT-12 | 종료된 공연 상태 | POLICY | COMMON | EVENT-12 | IMPLEMENTED | `EventStatus.CLOSED` |
| EVENT-13 | 지난 공연 조회 | GUIDE | COMMON | EVENT-13 | IMPLEMENTED | `/events/past` 화면 |
| EVENT-14 | 공연 랭킹 조회 기준 | GUIDE | COMMON | EVENT-14 | IMPLEMENTED | GET `/events/ranking` |
| EVENT-15 | 존재하지 않는 공연 안내 | ERROR_GUIDE | COMMON | EVENT-15 | IMPLEMENTED | 3001 상태코드 |

## VENUE — 공연장·회차·판매 좌석 (8)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| VENUE-01 | 공연장과 공연의 차이 | POLICY | COMMON | VENUE-01 | IMPLEMENTED | Venue/Event 모델 |
| VENUE-02 | 공연장의 좌석 행과 열 | POLICY | ADMIN | VENUE-02 | IMPLEMENTED | `VenueCreateDto` |
| VENUE-03 | VIP·R·S·A 좌석 등급 | POLICY | COMMON | VENUE-03 | IMPLEMENTED | `SeatGrade` |
| VENUE-04 | 공연 회차별 장소와 시간 | POLICY | COMMON | VENUE-04 | IMPLEMENTED | `EventScheduleResponseDto` |
| VENUE-05 | 같은 공연의 여러 회차 | FAQ | CUSTOMER | VENUE-04 | IMPLEMENTED | 회차 목록 API |
| VENUE-06 | 회차별 좌석과 가격 | POLICY | COMMON | VENUE-06 | IMPLEMENTED | `EventSeatResponseDto` |
| VENUE-07 | 존재하지 않는 공연장 안내 | ERROR_GUIDE | COMMON | VENUE-07 | IMPLEMENTED | 3004 상태코드 |
| VENUE-08 | 존재하지 않는 좌석 안내 | ERROR_GUIDE | CUSTOMER | VENUE-08 | IMPLEMENTED | 4009 상태코드 |

## QUEUE·SEAT — 대기열·좌석 (기존 정책 9)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| QUEUE-01 | 대기열 진입 | POLICY | CUSTOMER | QUEUE-01 | IMPLEMENTED | 기존 정책, `QueueService` |
| QUEUE-02 | 대기 순서 결정 | POLICY | CUSTOMER | QUEUE-02 | IMPLEMENTED | 기존 정책, Redis 순번 |
| QUEUE-03 | 대기 상태 갱신과 자동 입장 | POLICY | CUSTOMER | QUEUE-03 | IMPLEMENTED | 기존 정책, 3초 승급 주기 |
| QUEUE-04 | 예매 화면 입장 권한 유지 시간 | POLICY | CUSTOMER | QUEUE-04 | IMPLEMENTED | 기존 정책, Active TTL 10분 |
| QUEUE-05 | 새로고침과 중복 대기열 진입 | POLICY | CUSTOMER | QUEUE-05 | IMPLEMENTED | 기존 정책, 회원별 Redis 상태 |
| SEAT-01 | 화면 선택과 좌석 선점의 차이 | POLICY | CUSTOMER | SEAT-01 | IMPLEMENTED | 기존 정책, 예매 생성 시 선점 |
| SEAT-02 | 좌석 임시 선점 시간 | POLICY | CUSTOMER | SEAT-02 | IMPLEMENTED | 기존 정책, TTL 7분 |
| SEAT-03 | 동일 좌석에 대한 동시 요청 | POLICY | CUSTOMER | SEAT-03 | IMPLEMENTED | 기존 정책, `setIfAbsent` |
| SEAT-04 | 좌석 선점 만료와 자동 해제 | POLICY | CUSTOMER | SEAT-04 | IMPLEMENTED | 기존 정책, Redis TTL |

## RESERVATION·PAYMENT — 예매·결제 (기존 정책 6)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| RESERVATION-01 | 예매 확정 기준 | POLICY | CUSTOMER | RESERVATION-01 | IMPLEMENTED | 기존 정책, 결제 후 CONFIRMED |
| RESERVATION-02 | 좌석 선택 한도 | POLICY | CUSTOMER | RESERVATION-02 | IMPLEMENTED | 기존 정책, 최대 3개 |
| RESERVATION-03 | 공연 시작 후 신규 예매 제한 | POLICY | CUSTOMER | RESERVATION-03 | IMPLEMENTED | 기존 정책, 4012 상태코드 |
| RESERVATION-04 | 좌석 또는 회차 변경 | POLICY | CUSTOMER | RESERVATION-04 | PLANNED | 기존 정책, 직접 변경 API 없음 |
| PAYMENT-01 | 결제 실패와 재시도 | POLICY | CUSTOMER | PAYMENT-01 | IMPLEMENTED | 기존 정책, 선점 유효성 검사 |
| PAYMENT-02 | 동일 예매의 중복 결제 요청 | POLICY | CUSTOMER | PAYMENT-02 | IMPLEMENTED | 기존 정책, 5003 상태코드 |

## REFUND — 취소·환불 (기존 정책 9)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| REFUND-01 | 취소 가능 기간 | POLICY | CUSTOMER | REFUND-01 | PLANNED | 기존 정책, 날짜 제한 미구현 |
| REFUND-02 | 취소 수수료 | POLICY | CUSTOMER | REFUND-02 | PLANNED | 기존 정책, 계산 로직 미구현 |
| REFUND-03 | 예매 후 24시간 이내 취소 예외 | POLICY | CUSTOMER | REFUND-03 | PLANNED | 기존 정책, 예외 로직 미구현 |
| REFUND-04 | 수수료와 환불 예정액 계산 기준 | POLICY | CUSTOMER | REFUND-04 | PLANNED | 기존 정책, 계산 로직 미구현 |
| REFUND-05 | 주최 측의 공연 취소 | POLICY | CUSTOMER | REFUND-05 | PLANNED | 기존 정책 |
| REFUND-06 | 공연 일정 또는 장소 변경 | POLICY | CUSTOMER | REFUND-06 | PLANNED | 기존 정책 |
| REFUND-07 | 환불 수단과 처리 기간 | POLICY | CUSTOMER | REFUND-07 | PLANNED | 기존 정책 |
| REFUND-08 | 일부 좌석 취소 | POLICY | CUSTOMER | REFUND-08 | IMPLEMENTED | 예매 전체 취소 API만 존재 |
| REFUND-09 | 완료된 취소의 철회 | POLICY | CUSTOMER | REFUND-09 | IMPLEMENTED | 취소 상태 역전 API 없음 |

## RESERVATION GUIDE — 예매 이용·오류 (12)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| GUIDE-RES-01 | 좌석을 선택하지 않은 예매 요청 | ERROR_GUIDE | CUSTOMER | RESERVATION-05 | IMPLEMENTED | 4006 상태코드 |
| GUIDE-RES-02 | 다른 회차의 좌석을 선택한 경우 | ERROR_GUIDE | CUSTOMER | RESERVATION-06 | IMPLEMENTED | 4011 상태코드 |
| GUIDE-RES-03 | 이미 판매된 좌석을 선택한 경우 | ERROR_GUIDE | CUSTOMER | SEAT-03 | IMPLEMENTED | 4010 상태코드 |
| GUIDE-RES-04 | 다른 회원이 선점 중인 좌석 | ERROR_GUIDE | CUSTOMER | SEAT-03 | IMPLEMENTED | 4003 상태코드 |
| GUIDE-RES-05 | 예매 요청을 다시 보내도 중복 생성되지 않는 이유 | FAQ | CUSTOMER | RESERVATION-01 | IMPLEMENTED | Idempotency Key |
| GUIDE-RES-06 | 내 예매 목록에서 확인할 수 있는 정보 | GUIDE | CUSTOMER | RESERVATION-01 | IMPLEMENTED | `ReservationResponseDto` |
| GUIDE-RES-07 | 결제 대기·완료·취소 상태 | GUIDE | CUSTOMER | RESERVATION-01 | IMPLEMENTED | `ReservationStatus` |
| GUIDE-RES-08 | 본인 예매만 조회하고 취소할 수 있는 이유 | POLICY | CUSTOMER | RESERVATION-07 | IMPLEMENTED | 4005 상태코드 |
| GUIDE-RES-09 | 기타 취소 사유의 상세 내용 입력 | GUIDE | CUSTOMER | REFUND-08 | IMPLEMENTED | 4008 상태코드 |
| GUIDE-RES-10 | 결제 전에 선점 시간이 끝난 경우 | ERROR_GUIDE | CUSTOMER | SEAT-04 | IMPLEMENTED | 4007 상태코드 |
| GUIDE-RES-11 | 결제가 끝난 좌석의 상태 | FAQ | CUSTOMER | RESERVATION-01 | IMPLEMENTED | 좌석 RESERVED 전환 |
| GUIDE-RES-12 | 예매 취소 시 좌석과 결제 처리 | GUIDE | CUSTOMER | RESERVATION-08 | IMPLEMENTED | `ReservationService.cancel` |

## COUPON — 선착순 쿠폰 (8)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| COUPON-01 | 쿠폰 목록 조회 | GUIDE | CUSTOMER | COUPON-01 | IMPLEMENTED | GET `/coupons` |
| COUPON-02 | 선착순 쿠폰 발급 방법 | GUIDE | CUSTOMER | COUPON-02 | IMPLEMENTED | POST `/coupons/{id}/issue` |
| COUPON-03 | 한 사람이 같은 쿠폰을 중복 발급받을 수 없는 이유 | POLICY | CUSTOMER | COUPON-03 | IMPLEMENTED | 8002 상태코드 |
| COUPON-04 | 쿠폰이 모두 소진된 경우 | ERROR_GUIDE | CUSTOMER | COUPON-04 | IMPLEMENTED | 8003 상태코드 |
| COUPON-05 | 내 쿠폰 목록 조회 | GUIDE | CUSTOMER | COUPON-05 | IMPLEMENTED | GET `/coupons/me` |
| COUPON-06 | 정액 할인과 정률 할인 | POLICY | CUSTOMER | COUPON-06 | IMPLEMENTED | `DiscountType` |
| COUPON-07 | 쿠폰 발급 중 오류가 발생한 경우의 재고 복구 | POLICY | CUSTOMER | COUPON-07 | IMPLEMENTED | Rollback 보상 |
| COUPON-08 | 쿠폰을 결제에 사용하는 기능 | NOTICE | CUSTOMER | COUPON-08 | PLANNED | Payment DTO에 couponId 없음 |

## REVIEW — 공연 후기 (7)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| REVIEW-01 | 예매한 공연에만 후기를 작성할 수 있는 이유 | POLICY | CUSTOMER | REVIEW-01 | IMPLEMENTED | 7005 상태코드 |
| REVIEW-02 | 한 공연에 한 번만 후기 작성 | POLICY | CUSTOMER | REVIEW-02 | IMPLEMENTED | 7003 상태코드 |
| REVIEW-03 | 별점 범위 | POLICY | CUSTOMER | REVIEW-03 | IMPLEMENTED | 1~5 Validation |
| REVIEW-04 | 후기 글자 수 제한 | POLICY | CUSTOMER | REVIEW-04 | IMPLEMENTED | 최대 1000자 |
| REVIEW-05 | 최신순과 평점순 후기 조회 | GUIDE | COMMON | REVIEW-05 | IMPLEMENTED | Review sort 분기 |
| REVIEW-06 | 내가 작성한 후기 조회 | GUIDE | CUSTOMER | REVIEW-06 | IMPLEMENTED | GET `/me/reviews` |
| REVIEW-07 | 본인 후기만 삭제 가능 | POLICY | CUSTOMER | REVIEW-07 | IMPLEMENTED | 7004 상태코드 |

## SELLER — 판매자 공연 운영 (12)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| SELLER-01 | 판매자 공연 등록 | GUIDE | SELLER | SELLER-01 | IMPLEMENTED | POST `/events` |
| SELLER-02 | 공연 기간 입력 기준 | POLICY | SELLER | SELLER-02 | IMPLEMENTED | 3008 상태코드 |
| SELLER-03 | 공연 제목·설명·상영시간·출연진 입력 | GUIDE | SELLER | SELLER-03 | IMPLEMENTED | `EventCreateDto` |
| SELLER-04 | 공연 등록 후 검수 대기 | POLICY | SELLER | EVENT-09 | IMPLEMENTED | PENDING 초기 상태 |
| SELLER-05 | 승인된 공연 게시 | POLICY | SELLER | EVENT-10 | IMPLEMENTED | `AdminEventService.approve` |
| SELLER-06 | 공연 반려 사유 확인 | GUIDE | SELLER | EVENT-11 | IMPLEMENTED | Reject reason history |
| SELLER-07 | 본인이 등록한 공연만 수정 가능 | POLICY | SELLER | SELLER-07 | IMPLEMENTED | 3007 상태코드 |
| SELLER-08 | 본인이 등록한 공연만 삭제 가능 | POLICY | SELLER | SELLER-08 | IMPLEMENTED | `EventService.delete` |
| SELLER-09 | 공연 회차 등록 | GUIDE | SELLER | SELLER-09 | IMPLEMENTED | POST `/events/{id}/schedules` |
| SELLER-10 | 모든 좌석 등급의 가격 입력 | POLICY | SELLER | SELLER-10 | IMPLEMENTED | 3009 상태코드 |
| SELLER-11 | 공연 포스터 이미지 조건 | POLICY | SELLER | SELLER-11 | IMPLEMENTED | 이미지, 최대 5MB |
| SELLER-12 | 포스터 업로드 실패 대응 | ERROR_GUIDE | SELLER | SELLER-11 | IMPLEMENTED | 9004~9007 상태코드 |

## SETTLEMENT — 판매자 정산 (9)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| SETTLEMENT-01 | 판매자별 정산 목록 | GUIDE | SELLER | SETTLEMENT-01 | IMPLEMENTED | GET `/sellers/me/settlements` |
| SETTLEMENT-02 | 공연별 정산 금액 구성 | POLICY | SELLER | SETTLEMENT-02 | IMPLEMENTED | gross, commission, net |
| SETTLEMENT-03 | 정산 건별 결제 명세 | GUIDE | SELLER | SETTLEMENT-03 | IMPLEMENTED | settlement details API |
| SETTLEMENT-04 | 정산 상세 검색 조건 | GUIDE | SELLER | SETTLEMENT-04 | IMPLEMENTED | `SettlementDetailSearchCond` |
| SETTLEMENT-05 | 판매자 등급별 수수료율 | POLICY | SELLER | SETTLEMENT-05 | IMPLEMENTED | `SellerGrade` |
| SETTLEMENT-06 | 삭제된 공연의 정산 표시 | FAQ | SELLER | SETTLEMENT-06 | IMPLEMENTED | `(삭제된 공연)` 대체 제목 |
| SETTLEMENT-07 | 본인 정산만 조회 가능 | POLICY | SELLER | SETTLEMENT-07 | IMPLEMENTED | 5006 상태코드 |
| SETTLEMENT-08 | 취소 결제가 정산에 반영되는 과정 | GUIDE | SELLER | SETTLEMENT-08 | IMPLEMENTED | Outbox 소비 후 dirty 처리 |
| SETTLEMENT-09 | 존재하지 않는 정산 내역 | ERROR_GUIDE | SELLER | SETTLEMENT-09 | IMPLEMENTED | 5005 상태코드 |

## ADMIN — 관리자 이용 안내 (8)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| ADMIN-01 | 검수 대기 공연 조회 | GUIDE | ADMIN | ADMIN-01 | IMPLEMENTED | GET `/admin/events/pending` |
| ADMIN-02 | 공연 승인 처리 | GUIDE | ADMIN | ADMIN-02 | IMPLEMENTED | POST approve |
| ADMIN-03 | 공연 반려와 반려 사유 | GUIDE | ADMIN | ADMIN-03 | IMPLEMENTED | POST reject, 사유 필수 |
| ADMIN-04 | 회원 검색과 페이징 | GUIDE | ADMIN | ADMIN-04 | IMPLEMENTED | `AdminMemberController` |
| ADMIN-05 | 회원 정지와 사유 기록 | GUIDE | ADMIN | ADMIN-05 | IMPLEMENTED | suspend API, history |
| ADMIN-06 | 회원 정지 해제 | GUIDE | ADMIN | ADMIN-06 | IMPLEMENTED | release API |
| ADMIN-07 | 공연장 등록·수정·삭제 | GUIDE | ADMIN | ADMIN-07 | IMPLEMENTED | `VenueController` |
| ADMIN-08 | 일별 매출 통계와 재집계 | GUIDE | ADMIN | ADMIN-08 | IMPLEMENTED | `AdminStatsController` |

## COMMON — 공통 오류·이용 안내 (8)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 | 근거 |
|---|---|---|---|---|---|---|
| COMMON-01 | 잘못된 입력값 오류 | ERROR_GUIDE | COMMON | COMMON-01 | IMPLEMENTED | 9002 상태코드 |
| COMMON-02 | 이미 처리된 요청 | ERROR_GUIDE | COMMON | COMMON-02 | IMPLEMENTED | 9008 상태코드 |
| COMMON-03 | 서버 오류가 발생한 경우 | ERROR_GUIDE | COMMON | COMMON-03 | IMPLEMENTED | 9001 상태코드 |
| COMMON-04 | 존재하지 않는 정보를 조회한 경우 | ERROR_GUIDE | COMMON | COMMON-04 | IMPLEMENTED | 도메인별 NOT_FOUND |
| COMMON-05 | 현재 상태에서 처리할 수 없는 요청 | ERROR_GUIDE | COMMON | COMMON-05 | IMPLEMENTED | 상태 전이 검증 |
| COMMON-06 | 목록 조회의 페이지 이동 | GUIDE | COMMON | COMMON-06 | IMPLEMENTED | Pageable API |
| COMMON-07 | 검색 결과가 비어 있는 경우 | GUIDE | COMMON | COMMON-07 | IMPLEMENTED | 목록 Empty UI |
| COMMON-08 | 로그인 페이지로 다시 이동한 경우 | FAQ | COMMON | AUTH-02 | IMPLEMENTED | 401 인터셉터·라우터 가드 |

## 현실형 FAQ·경쟁 문서 (35)

| chunkId | 제목 | 유형 | 대상 | 원천 | 상태 |
|---|---|---|---|---|---|
| FAQ-AUTH-01 | 자꾸 로그인이 풀리는 이유 | FAQ | COMMON | AUTH-02 | IMPLEMENTED |
| FAQ-AUTH-02 | 로그아웃했는데 뒤로 가면 화면이 보이는 경우 | FAQ | COMMON | AUTH-04 | IMPLEMENTED |
| FAQ-AUTH-03 | 로그인했는데 판매자 화면에 못 들어가는 이유 | FAQ | COMMON | AUTH-07 | IMPLEMENTED |
| FAQ-MEMBER-01 | 같은 이메일로 다시 가입할 수 없는 이유 | FAQ | COMMON | MEMBER-03 | IMPLEMENTED |
| FAQ-MEMBER-02 | 닉네임 변경 중 중복 오류가 나는 경우 | FAQ | CUSTOMER | MEMBER-04 | IMPLEMENTED |
| FAQ-MEMBER-03 | 탈퇴한 계정으로 다시 로그인할 수 없는 이유 | FAQ | COMMON | MEMBER-08 | IMPLEMENTED |
| FAQ-EVENT-01 | 검색한 공연이 목록에 안 보이는 이유 | FAQ | COMMON | EVENT-10 | IMPLEMENTED |
| FAQ-EVENT-02 | 지난 공연을 어디서 보는지 | FAQ | COMMON | EVENT-13 | IMPLEMENTED |
| FAQ-EVENT-03 | 공연 관람등급은 어디서 확인하는지 | FAQ | COMMON | EVENT-04 | IMPLEMENTED |
| FAQ-VENUE-01 | 같은 공연인데 날짜마다 공연장이 다른 경우 | FAQ | CUSTOMER | VENUE-04 | IMPLEMENTED |
| FAQ-VENUE-02 | 같은 좌석 번호인데 가격이 다른 이유 | FAQ | CUSTOMER | VENUE-06 | IMPLEMENTED |
| FAQ-QUEUE-01 | 친구와 같이 눌렀는데 순번이 다른 이유 | FAQ | CUSTOMER | QUEUE-02 | IMPLEMENTED |
| FAQ-QUEUE-02 | 대기 화면을 계속 켜둬야 하는지 | FAQ | CUSTOMER | QUEUE-03 | IMPLEMENTED |
| FAQ-QUEUE-03 | 줄을 통과한 뒤 잠시 자리를 비워도 되는지 | FAQ | CUSTOMER | QUEUE-04 | IMPLEMENTED |
| FAQ-QUEUE-04 | 새로고침하면 순번이 빨라지는지 | FAQ | CUSTOMER | QUEUE-05 | IMPLEMENTED |
| FAQ-SEAT-01 | 좌석을 눌렀는데 다른 사람이 가져간 이유 | FAQ | CUSTOMER | SEAT-01 | IMPLEMENTED |
| FAQ-SEAT-02 | 자리를 잡고 결제를 미루면 어떻게 되는지 | FAQ | CUSTOMER | SEAT-02 | IMPLEMENTED |
| FAQ-SEAT-03 | 먼저 클릭했는데 선점에 실패한 이유 | FAQ | CUSTOMER | SEAT-03 | IMPLEMENTED |
| FAQ-SEAT-04 | 결제 화면에서 자리가 사라진 이유 | FAQ | CUSTOMER | SEAT-04 | IMPLEMENTED |
| FAQ-RES-01 | 예매됐다고 떴는데 결제 완료가 아닌 이유 | FAQ | CUSTOMER | RESERVATION-01 | IMPLEMENTED |
| FAQ-RES-02 | 네 명이 한 번에 예매할 수 없는 이유 | FAQ | CUSTOMER | RESERVATION-02 | IMPLEMENTED |
| FAQ-RES-03 | 공연이 시작된 뒤 빈 좌석을 살 수 없는 이유 | FAQ | CUSTOMER | RESERVATION-03 | IMPLEMENTED |
| FAQ-PAY-01 | 카드 오류 후 같은 예매로 다시 결제하는 방법 | FAQ | CUSTOMER | PAYMENT-01 | IMPLEMENTED |
| FAQ-PAY-02 | 결제 버튼을 여러 번 누른 경우 | FAQ | CUSTOMER | PAYMENT-02 | IMPLEMENTED |
| FAQ-REFUND-01 | 여러 좌석 중 한 자리만 취소할 수 없는 이유 | FAQ | CUSTOMER | REFUND-08 | IMPLEMENTED |
| FAQ-REFUND-02 | 취소 완료를 되돌릴 수 없는 이유 | FAQ | CUSTOMER | REFUND-09 | IMPLEMENTED |
| FAQ-COUPON-01 | 쿠폰 받기를 눌렀는데 이미 받았다고 나오는 경우 | FAQ | CUSTOMER | COUPON-03 | IMPLEMENTED |
| FAQ-COUPON-02 | 쿠폰이 순식간에 품절된 이유 | FAQ | CUSTOMER | COUPON-04 | IMPLEMENTED |
| FAQ-REVIEW-01 | 공연을 봤는데 후기를 작성할 수 없는 경우 | FAQ | CUSTOMER | REVIEW-01 | IMPLEMENTED |
| FAQ-REVIEW-02 | 같은 공연 후기를 또 쓸 수 없는 이유 | FAQ | CUSTOMER | REVIEW-02 | IMPLEMENTED |
| FAQ-SELLER-01 | 등록한 공연이 바로 공개되지 않는 이유 | FAQ | SELLER | EVENT-09 | IMPLEMENTED |
| FAQ-SELLER-02 | 공연 수정 권한이 없다고 나오는 이유 | FAQ | SELLER | SELLER-07 | IMPLEMENTED |
| FAQ-SELLER-03 | 포스터 업로드가 거절되는 이유 | FAQ | SELLER | SELLER-11 | IMPLEMENTED |
| FAQ-SETTLEMENT-01 | 결제금액과 입금 예정액이 다른 이유 | FAQ | SELLER | SETTLEMENT-02 | IMPLEMENTED |
| FAQ-SETTLEMENT-02 | 취소된 결제가 정산에서 빠지는 시점 | FAQ | SELLER | SETTLEMENT-08 | IMPLEMENTED |

## 집계

| 구분 | Chunk 수 |
|---|---:|
| 원천 정책·가이드·오류 안내 | 131 |
| 현실형 FAQ·경쟁 문서 | 35 |
| **총합** | **166** |

최종 문서 작성 전에 각 항목의 근거 코드와 현재 구현 상태를 다시 확인한다.
Manifest 승인 이후에만 실제 Markdown Chunk를 작성한다.
