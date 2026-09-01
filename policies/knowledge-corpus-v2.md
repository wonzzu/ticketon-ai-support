---
domain: KNOWLEDGE_CORPUS_V2
version: "2.0"
effectiveFrom: "2026-09-01"
status: ACTIVE
---

# TicketOn 고객지원 지식 문서 V2

TicketOn의 인증, 회원, 공연, 예매, 결제, 후기, 판매자 운영, 정산과 관리자 기능을 안내합니다.
개인의 실제 예매·결제·쿠폰·정산 상태는 이 문서가 아니라 본인 확인 후 서비스 조회가 필요합니다.

## AUTH-01 로그인에 필요한 정보

sourcePolicyId: AUTH-01
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: AUTH

로그인할 때는 가입한 이메일과 비밀번호를 입력해야 합니다. 정보가 일치하지 않으면 로그인할 수 없습니다.

## AUTH-02 Access Token이 만료된 경우

sourcePolicyId: AUTH-02
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: AUTH

Access Token이 만료되면 Refresh Token으로 새 Access Token 발급을 시도합니다. 갱신할 수 없으면 다시 로그인해야 합니다.

## AUTH-03 Refresh Token으로 로그인 연장

sourcePolicyId: AUTH-03
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: AUTH

유효한 Refresh Token이 있으면 로그인 상태를 연장할 수 있습니다. Refresh Token까지 만료되었거나 무효하면 다시 로그인해야 합니다.

## AUTH-04 로그아웃 후 토큰 처리

sourcePolicyId: AUTH-04
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: AUTH

로그아웃하면 서버에 저장된 Refresh Token이 제거되어 이후 토큰 갱신에 사용할 수 없습니다. 공용 기기에서는 로그아웃 후 브라우저도 닫는 것이 안전합니다.

## AUTH-05 로그인하지 않고 이용할 수 있는 기능

sourcePolicyId: AUTH-05
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: AUTH

공연 목록·검색·상세와 공개 후기 등은 로그인하지 않아도 볼 수 있습니다. 예매, 내 정보, 쿠폰 발급처럼 회원 식별이 필요한 기능은 로그인이 필요합니다.

## AUTH-06 일반 회원 전용 기능

sourcePolicyId: AUTH-06
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: AUTH

일반 회원은 예매, 결제, 내 예매·쿠폰·후기 관리 기능을 이용할 수 있습니다. 판매자나 관리자 전용 화면은 일반 회원 권한으로 접근할 수 없습니다.

## AUTH-07 판매자 전용 기능

sourcePolicyId: AUTH-07
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: AUTH

공연 등록과 판매자 정산 같은 기능은 판매자 계정만 이용할 수 있습니다. 일반 회원으로 로그인했다면 판매자 전용 화면에 접근할 수 없습니다.

## AUTH-08 관리자 전용 기능

sourcePolicyId: AUTH-08
documentType: POLICY
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: AUTH

공연 검수, 회원 제재, 공연장 관리와 통계 재집계는 관리자 전용 기능입니다. 관리자 권한이 없는 계정의 요청은 거부됩니다.

## AUTH-09 인증 필요 오류와 권한 없음 오류의 차이

sourcePolicyId: AUTH-09
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: AUTH

로그인 정보가 없거나 만료된 경우에는 인증 필요 오류가 발생합니다. 로그인은 되었지만 역할이나 소유 권한이 부족하면 권한 없음 오류가 발생합니다.

## AUTH-10 요청을 너무 많이 보낸 경우

sourcePolicyId: AUTH-10
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: AUTH

짧은 시간에 요청을 너무 많이 보내면 서비스 보호를 위해 일시적으로 요청이 제한될 수 있습니다. 잠시 기다린 뒤 다시 시도해야 합니다.

## MEMBER-01 일반 회원 가입 방법

sourcePolicyId: MEMBER-01
documentType: GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: MEMBER

일반 회원 가입 화면에서 이메일, 비밀번호, 닉네임 등 필수 정보를 입력해 가입합니다. 이미 사용 중인 이메일이나 닉네임은 사용할 수 없습니다.

## MEMBER-02 판매자 회원 가입 방법

sourcePolicyId: MEMBER-02
documentType: GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: MEMBER

판매자 가입 화면에서 계정 정보와 판매자 정보를 입력해 가입합니다. 가입 후 판매자 권한으로 공연 등록과 정산 기능을 이용할 수 있습니다.

## MEMBER-03 이미 사용 중인 이메일

sourcePolicyId: MEMBER-03
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: MEMBER

이미 가입에 사용된 이메일로는 새 계정을 만들 수 없습니다. 기존 계정으로 로그인하거나 다른 이메일을 사용해야 합니다.

## MEMBER-04 이미 사용 중인 닉네임

sourcePolicyId: MEMBER-04
documentType: ERROR_GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: MEMBER

다른 회원이 사용 중인 닉네임은 가입이나 정보 수정에 사용할 수 없습니다. 중복되지 않는 닉네임으로 변경해야 합니다.

## MEMBER-05 내 회원 정보 조회

sourcePolicyId: MEMBER-05
documentType: GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: MEMBER

로그인한 사용자는 내 정보 화면에서 자신의 회원 정보를 조회할 수 있습니다. 다른 회원의 개인 정보는 이 기능으로 조회할 수 없습니다.

## MEMBER-06 일반 회원 정보 수정 범위

sourcePolicyId: MEMBER-06
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: MEMBER

일반 회원은 내 정보 수정 기능에서 허용된 프로필 항목을 변경할 수 있습니다. 계정 역할이나 다른 회원 정보는 수정할 수 없습니다.

## MEMBER-07 판매자 정보 수정 범위

sourcePolicyId: MEMBER-07
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: MEMBER

판매자는 내 판매자 정보 수정 기능에서 허용된 사업자·프로필 정보를 변경할 수 있습니다. 다른 판매자의 정보는 수정할 수 없습니다.

## MEMBER-08 회원 탈퇴 처리

sourcePolicyId: MEMBER-08
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: MEMBER

회원 탈퇴를 요청하면 계정이 탈퇴 상태로 처리되어 기존 방식으로 로그인하거나 회원 전용 기능을 이용할 수 없습니다. 탈퇴 전에 필요한 예매와 정산 정보를 확인해야 합니다.

## MEMBER-09 회원 상태 종류와 이용 제한

sourcePolicyId: MEMBER-09
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: MEMBER

회원 계정은 정상, 정지, 탈퇴 등 상태에 따라 이용 가능 범위가 달라집니다. 정상 상태가 아니면 로그인이나 일부 기능이 제한될 수 있습니다.

## MEMBER-10 정지 회원의 이용과 해제

sourcePolicyId: MEMBER-10
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: MEMBER

정지된 회원은 정지 기간 또는 관리자 해제 전까지 제한된 기능을 이용할 수 없습니다. 정지 해제는 관리자가 사유와 상태를 확인한 뒤 처리합니다.

## EVENT-01 공연 목록 조회

sourcePolicyId: EVENT-01
documentType: GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

공연 목록에서 현재 조회 가능한 공연을 페이지 단위로 확인할 수 있습니다. 원하는 공연을 찾기 위해 검색과 카테고리 조건을 함께 사용할 수 있습니다.

## EVENT-02 공연 제목으로 검색

sourcePolicyId: EVENT-02
documentType: GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

공연 검색창에 제목의 전체 또는 일부를 입력해 공연을 찾을 수 있습니다. 검색 결과가 없다면 철자와 게시 상태를 확인해야 합니다.

## EVENT-03 공연 카테고리로 검색

sourcePolicyId: EVENT-03
documentType: GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

콘서트, 뮤지컬 등 제공되는 카테고리 조건으로 공연 목록을 좁힐 수 있습니다. 카테고리와 검색어를 함께 사용하면 원하는 공연을 더 쉽게 찾을 수 있습니다.

## EVENT-04 공연 상세 정보에 포함되는 항목

sourcePolicyId: EVENT-04
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

공연 상세에서는 제목, 설명, 관람 등급, 상영 시간, 출연진, 포스터와 회차 정보를 확인할 수 있습니다. 예매 전 선택한 회차의 장소와 시간을 다시 확인해야 합니다.

## EVENT-05 전체 관람가 공연

sourcePolicyId: EVENT-05
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

전체 관람가로 표시된 공연은 별도의 연령 하한 없이 관람할 수 있습니다. 현장 운영에 필요한 증빙이나 보호자 안내가 별도로 공지되면 해당 안내를 따릅니다.

## EVENT-06 12세 이상 관람가

sourcePolicyId: EVENT-06
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

12세 이상 관람가 공연은 만 12세 이상 관람객을 대상으로 합니다. 예매 전 공연 상세의 관람 등급을 확인해야 합니다.

## EVENT-07 15세 이상 관람가

sourcePolicyId: EVENT-07
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

15세 이상 관람가 공연은 만 15세 이상 관람객을 대상으로 합니다. 현장에서 연령 확인을 요청할 수 있으므로 공연 안내를 확인해야 합니다.

## EVENT-08 18세 이상 관람가

sourcePolicyId: EVENT-08
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

18세 이상 관람가 공연은 만 18세 이상 관람객만 관람할 수 있습니다. 연령 확인이 되지 않으면 입장이 제한될 수 있습니다.

## EVENT-09 공연 검수 대기 상태

sourcePolicyId: EVENT-09
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: EVENT

새로 등록된 공연은 관리자의 검수가 끝나기 전까지 검수 대기 상태입니다. 이 상태에서는 일반 고객에게 판매 공연으로 게시되지 않습니다.

## EVENT-10 게시 중인 공연 상태

sourcePolicyId: EVENT-10
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

관리자 승인을 받아 게시 중인 공연만 일반 고객의 공연 목록과 검색 결과에 노출됩니다. 반려되거나 종료된 공연은 일반 판매 목록에서 보이지 않을 수 있습니다.

## EVENT-11 반려된 공연 상태

sourcePolicyId: EVENT-11
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: EVENT

검수에서 반려된 공연은 판매자가 반려 사유를 확인하고 내용을 보완해야 합니다. 반려 상태인 동안 일반 고객에게 판매 공연으로 게시되지 않습니다.

## EVENT-12 종료된 공연 상태

sourcePolicyId: EVENT-12
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

공연 기간이 끝나 종료 상태가 된 공연은 신규 예매 대상이 아닙니다. 지난 공연 조회 화면에서는 기록을 확인할 수 있습니다.

## EVENT-13 지난 공연 조회

sourcePolicyId: EVENT-13
documentType: GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

종료된 공연은 지난 공연 메뉴에서 조회할 수 있습니다. 현재 판매 중인 공연 목록과 분리되어 표시됩니다.

## EVENT-14 공연 랭킹 조회 기준

sourcePolicyId: EVENT-14
documentType: GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

공연 랭킹 화면은 서비스가 집계한 순위 기준에 따라 공연을 보여줍니다. 집계 시점에 따라 화면의 순위가 달라질 수 있습니다.

## EVENT-15 존재하지 않는 공연 안내

sourcePolicyId: EVENT-15
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: EVENT

요청한 공연 ID가 없거나 삭제된 경우 공연을 찾을 수 없다는 오류가 발생합니다. 목록이나 검색 화면에서 현재 존재하는 공연을 다시 선택해야 합니다.

## VENUE-01 공연장과 공연의 차이

sourcePolicyId: VENUE-01
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: VENUE

공연장은 좌석 배치가 정의된 장소이고, 공연은 그 장소에서 열리는 콘텐츠입니다. 한 공연은 회차에 따라 공연장과 시간이 정해집니다.

## VENUE-02 공연장의 좌석 행과 열

sourcePolicyId: VENUE-02
documentType: POLICY
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: VENUE

관리자는 공연장을 등록할 때 좌석 배치를 구성하는 행과 열 정보를 입력합니다. 이 정보는 공연 회차의 판매 좌석을 구성하는 기준이 됩니다.

## VENUE-03 VIP·R·S·A 좌석 등급

sourcePolicyId: VENUE-03
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: VENUE

좌석은 VIP, R, S, A 등급으로 구분될 수 있습니다. 등급별 가격은 공연 회차 설정에 따라 달라질 수 있습니다.

## VENUE-04 공연 회차별 장소와 시간

sourcePolicyId: VENUE-04
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: VENUE

공연의 각 회차에는 별도의 시작 시간과 공연장이 지정됩니다. 예매할 때 날짜뿐 아니라 선택한 회차의 장소와 시간을 확인해야 합니다.

## VENUE-05 같은 공연의 여러 회차

sourcePolicyId: VENUE-04
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: VENUE

같은 공연이라도 날짜와 시간별로 여러 회차가 열릴 수 있습니다. 좌석과 가격은 선택한 회차를 기준으로 조회해야 합니다.

## VENUE-06 회차별 좌석과 가격

sourcePolicyId: VENUE-06
documentType: POLICY
audience: COMMON
implementationStatus: IMPLEMENTED
domain: VENUE

판매 좌석의 등급과 가격은 공연 회차별로 설정됩니다. 같은 좌석 번호라도 회차나 공연이 다르면 가격이 달라질 수 있습니다.

## VENUE-07 존재하지 않는 공연장 안내

sourcePolicyId: VENUE-07
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: VENUE

요청한 공연장이 없거나 삭제되었다면 공연장 정보를 찾을 수 없다는 오류가 발생합니다. 현재 등록된 공연장을 다시 선택해야 합니다.

## VENUE-08 존재하지 않는 좌석 안내

sourcePolicyId: VENUE-08
documentType: ERROR_GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: VENUE

요청한 좌석이 해당 회차에 없으면 좌석을 찾을 수 없다는 오류가 발생합니다. 좌석 화면을 새로 조회한 뒤 표시되는 좌석을 선택해야 합니다.

## GUIDE-RES-01 좌석을 선택하지 않은 예매 요청

sourcePolicyId: RESERVATION-05
documentType: ERROR_GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

예매 요청에는 한 개 이상의 좌석을 선택해야 합니다. 좌석을 고르지 않고 예매를 요청하면 처리되지 않으므로 현재 회차의 판매 가능한 좌석을 선택한 뒤 다시 요청해야 합니다.

## GUIDE-RES-02 다른 회차의 좌석을 선택한 경우

sourcePolicyId: RESERVATION-06
documentType: ERROR_GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

예매하려는 공연 회차와 선택한 좌석의 회차는 같아야 합니다. 다른 날짜나 시간의 회차에 속한 좌석을 섞으면 예매가 거부되므로 회차를 다시 확인하고 그 회차의 좌석만 선택해야 합니다.

## GUIDE-RES-03 이미 판매된 좌석을 선택한 경우

sourcePolicyId: SEAT-03
documentType: ERROR_GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

이미 결제가 완료되어 판매된 좌석은 새 예매에 사용할 수 없습니다. 좌석 화면을 새로 조회하고 아직 판매 가능한 다른 좌석을 선택해야 합니다.

## GUIDE-RES-04 다른 회원이 선점 중인 좌석

sourcePolicyId: SEAT-03
documentType: ERROR_GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

다른 회원이 임시 선점한 좌석은 선점이 유지되는 동안 예매할 수 없습니다. 최신 좌석 상태를 확인해 다른 좌석을 고르거나 기존 선점이 만료된 뒤 다시 확인해야 합니다.

## GUIDE-RES-05 예매 요청을 다시 보내도 중복 생성되지 않는 이유

sourcePolicyId: RESERVATION-01
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

예매 요청에는 같은 요청을 구분하는 Idempotency Key를 사용합니다. 같은 키로 요청을 다시 보내면 별도의 예매를 중복 생성하지 않고 기존 처리 결과를 기준으로 응답합니다.

## GUIDE-RES-06 내 예매 목록에서 확인할 수 있는 정보

sourcePolicyId: RESERVATION-01
documentType: GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

내 예매 목록에서는 본인이 예매한 공연, 회차, 좌석, 결제금액과 예매 상태를 확인할 수 있습니다. 다른 회원의 예매는 내 목록에 표시되지 않습니다.

## GUIDE-RES-07 결제 대기·완료·취소 상태

sourcePolicyId: RESERVATION-01
documentType: GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

결제 대기는 좌석을 선점했지만 결제가 끝나지 않은 상태이고, 결제 완료는 결제까지 정상 처리된 상태입니다. 취소 상태는 예매 취소가 완료된 상태이며 완료된 취소를 이전 상태로 되돌릴 수 없습니다.

## GUIDE-RES-08 본인 예매만 조회하고 취소할 수 있는 이유

sourcePolicyId: RESERVATION-07
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

예매 조회와 취소는 해당 예매를 만든 회원 본인만 할 수 있습니다. 다른 회원의 예매 ID를 알고 있더라도 조회하거나 취소할 수 없으며 권한 오류로 처리됩니다.

## GUIDE-RES-09 기타 취소 사유의 상세 내용 입력

sourcePolicyId: REFUND-08
documentType: GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

취소 사유로 기타를 선택하면 구체적인 사유 내용을 함께 입력해야 합니다. 상세 내용이 없으면 취소 요청이 거부되므로 이유를 작성한 뒤 다시 요청해야 합니다.

## GUIDE-RES-10 결제 전에 선점 시간이 끝난 경우

sourcePolicyId: SEAT-04
documentType: ERROR_GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

결제 전에 좌석 선점 7분이 끝나면 해당 예매로 결제를 계속할 수 없습니다. 최신 좌석 상태를 다시 확인하고 좌석 선택과 예매 요청부터 새로 진행해야 합니다.

## GUIDE-RES-11 결제가 끝난 좌석의 상태

sourcePolicyId: RESERVATION-01
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

결제가 완료된 좌석은 판매 완료 상태로 바뀌어 다른 회원이 선택하거나 선점할 수 없습니다. 결제가 완료되지 않은 임시 선점 좌석과 구분해 표시됩니다.

## GUIDE-RES-12 예매 취소 시 좌석과 결제 처리

sourcePolicyId: RESERVATION-08
documentType: GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: RESERVATION

예매 전체 취소가 완료되면 연결된 결제는 취소 처리되고 예매에 포함된 좌석은 다시 판매 가능한 상태로 전환됩니다. 현재는 한 예매의 일부 좌석만 골라 취소할 수 없으므로 포함된 좌석 전체가 함께 처리됩니다.

## COUPON-01 쿠폰 목록 조회

sourcePolicyId: COUPON-01
documentType: GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: COUPON

쿠폰 목록에서 현재 발급 가능한 쿠폰과 기본 할인 정보를 확인할 수 있습니다. 실제 보유 쿠폰은 내 쿠폰 목록에서 따로 확인합니다.

## COUPON-02 선착순 쿠폰 발급 방법

sourcePolicyId: COUPON-02
documentType: GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: COUPON

발급 가능한 선착순 쿠폰에서 발급 버튼을 눌러 요청합니다. 남은 수량 안에서 발급에 성공한 회원만 해당 쿠폰을 보유하게 됩니다.

## COUPON-03 한 사람이 같은 쿠폰을 중복 발급받을 수 없는 이유

sourcePolicyId: COUPON-03
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: COUPON

한 회원은 같은 선착순 쿠폰을 한 번만 발급받을 수 있습니다. 이미 발급받았다면 다시 요청해도 중복 발급되지 않습니다.

## COUPON-04 쿠폰이 모두 소진된 경우

sourcePolicyId: COUPON-04
documentType: ERROR_GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: COUPON

준비된 수량이 모두 발급되면 쿠폰 소진 오류가 발생하며 더 이상 발급받을 수 없습니다. 화면의 잔여 수량은 동시 요청 때문에 즉시 달라질 수 있습니다.

## COUPON-05 내 쿠폰 목록 조회

sourcePolicyId: COUPON-05
documentType: GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: COUPON

로그인 후 내 쿠폰 목록에서 자신에게 발급된 쿠폰을 확인할 수 있습니다. 다른 회원의 쿠폰은 조회할 수 없습니다.

## COUPON-06 정액 할인과 정률 할인

sourcePolicyId: COUPON-06
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: COUPON

정액 할인은 정해진 금액을 빼고, 정률 할인은 기준 금액에 할인율을 적용합니다. 실제 할인 조건과 한도는 쿠폰 안내를 확인해야 합니다.

## COUPON-07 쿠폰 발급 중 오류가 발생한 경우의 재고 복구

sourcePolicyId: COUPON-07
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: COUPON

쿠폰 발급 처리 중 오류가 발생하면 차감된 재고를 복구해 실제 발급 수량과 재고가 어긋나지 않도록 처리합니다. 오류가 표시되면 발급 완료 여부를 내 쿠폰 목록에서 확인해야 합니다.

## COUPON-08 쿠폰을 결제에 사용하는 기능

sourcePolicyId: COUPON-08
documentType: NOTICE
audience: CUSTOMER
implementationStatus: PLANNED
domain: COUPON

현재 쿠폰 발급과 보유 목록 조회는 가능하지만 쿠폰을 결제 금액에 적용하는 기능은 아직 제공 예정입니다. 보유 쿠폰이 있어도 현재 결제에는 자동 적용되지 않습니다.

## REVIEW-01 예매한 공연에만 후기를 작성할 수 있는 이유

sourcePolicyId: REVIEW-01
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: REVIEW

실제로 예매한 공연에 대해서만 후기를 작성할 수 있습니다. 예매 내역이 확인되지 않으면 후기 등록이 거부됩니다.

## REVIEW-02 한 공연에 한 번만 후기 작성

sourcePolicyId: REVIEW-02
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: REVIEW

한 회원은 같은 공연에 후기를 한 번만 작성할 수 있습니다. 이미 작성한 후기가 있다면 중복 등록할 수 없습니다.

## REVIEW-03 별점 범위

sourcePolicyId: REVIEW-03
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: REVIEW

후기 별점은 1점부터 5점 사이로 입력해야 합니다. 범위를 벗어난 별점은 저장되지 않습니다.

## REVIEW-04 후기 글자 수 제한

sourcePolicyId: REVIEW-04
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: REVIEW

후기 내용은 최대 1000자까지 작성할 수 있습니다. 제한을 넘으면 내용을 줄인 뒤 다시 등록해야 합니다.

## REVIEW-05 최신순과 평점순 후기 조회

sourcePolicyId: REVIEW-05
documentType: GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: REVIEW

공연 후기는 최신순 또는 평점순으로 정렬해 볼 수 있습니다. 선택한 정렬 기준에 따라 표시 순서가 달라집니다.

## REVIEW-06 내가 작성한 후기 조회

sourcePolicyId: REVIEW-06
documentType: GUIDE
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: REVIEW

로그인 후 내가 작성한 후기 목록을 조회할 수 있습니다. 다른 회원이 작성한 후기는 내 후기 목록에 포함되지 않습니다.

## REVIEW-07 본인 후기만 삭제 가능

sourcePolicyId: REVIEW-07
documentType: POLICY
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: REVIEW

후기는 작성한 본인만 삭제할 수 있습니다. 다른 회원의 후기 삭제 요청은 권한 오류로 거부됩니다.

## SELLER-01 판매자 공연 등록

sourcePolicyId: SELLER-01
documentType: GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

판매자 계정은 공연 등록 화면에서 새 공연을 만들 수 있습니다. 공연 기본 정보, 기간, 포스터와 필요한 운영 정보를 입력해 등록하면 공연은 관리자 검수를 기다리는 상태가 됩니다.

## SELLER-02 공연 기간 입력 기준

sourcePolicyId: SELLER-02
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

공연 기간을 등록할 때 종료일은 시작일보다 빠를 수 없습니다. 날짜 순서가 올바르지 않으면 등록이 거부되므로 시작일과 종료일을 다시 확인해야 합니다.

## SELLER-03 공연 제목·설명·상영시간·출연진 입력

sourcePolicyId: SELLER-03
documentType: GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

공연 등록에는 제목, 설명, 상영시간과 출연진 등 고객에게 보여줄 기본 정보를 입력합니다. 필수 정보가 빠졌거나 입력 형식이 올바르지 않으면 공연을 등록할 수 없습니다.

## SELLER-04 공연 등록 후 검수 대기

sourcePolicyId: EVENT-09
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

판매자가 공연을 등록하면 처음에는 검수 대기 상태가 됩니다. 관리자가 내용을 승인하기 전에는 일반 고객의 판매 공연 목록과 검색 결과에 공개되지 않습니다.

## SELLER-05 승인된 공연 게시

sourcePolicyId: EVENT-10
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

관리자가 공연을 승인하면 게시 가능한 상태로 바뀌어 일반 고객이 목록과 검색에서 확인할 수 있습니다. 승인 전에 검수 대기 중이거나 반려된 공연은 판매 공연으로 노출되지 않습니다.

## SELLER-06 공연 반려 사유 확인

sourcePolicyId: EVENT-11
documentType: GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

공연이 반려되면 판매자는 자신의 공연 관리 화면에서 반려 사유를 확인할 수 있습니다. 사유에 맞게 공연 정보를 보완한 뒤 다시 검수를 받아야 합니다.

## SELLER-07 본인이 등록한 공연만 수정 가능

sourcePolicyId: SELLER-07
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

공연 정보는 그 공연을 등록한 판매자만 수정할 수 있습니다. 다른 판매자가 만든 공연을 수정하려고 하면 소유 권한이 없어 요청이 거부됩니다.

## SELLER-08 본인이 등록한 공연만 삭제 가능

sourcePolicyId: SELLER-08
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

공연 삭제는 해당 공연을 등록한 판매자만 요청할 수 있습니다. 다른 판매자의 공연은 삭제할 수 없으며 삭제된 공연의 기존 정산 기록은 별도로 유지될 수 있습니다.

## SELLER-09 공연 회차 등록

sourcePolicyId: SELLER-09
documentType: GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

판매자는 자신이 등록한 공연에 날짜와 시작 시간이 다른 회차를 여러 개 추가할 수 있습니다. 회차를 등록할 때 공연장과 공연 시작 일시를 지정하며 고객은 등록된 회차 중 원하는 날짜와 시간을 선택해 예매합니다.

## SELLER-10 모든 좌석 등급의 가격 입력

sourcePolicyId: SELLER-10
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

공연 회차의 판매 좌석을 구성할 때 사용되는 모든 좌석 등급의 가격을 입력해야 합니다. 일부 등급의 가격을 빠뜨리면 회차 등록이 거부되므로 VIP, R, S, A 등 실제 사용 등급을 모두 확인해야 합니다.

## SELLER-11 공연 포스터 이미지 조건

sourcePolicyId: SELLER-11
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

공연 포스터는 이미지 파일만 등록할 수 있으며 파일 크기는 최대 5MB입니다. 이미지가 아니거나 허용 크기를 넘으면 업로드할 수 없습니다.

## SELLER-12 포스터 업로드 실패 대응

sourcePolicyId: SELLER-11
documentType: ERROR_GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SELLER

포스터 업로드가 실패하면 파일이 이미지인지, 크기가 5MB 이하인지와 파일 저장 요청이 정상인지 확인해야 합니다. 조건을 고쳐 다시 시도하고 서버 저장 오류가 반복되면 고객지원에 문의해야 합니다.

## SETTLEMENT-01 판매자별 정산 목록

sourcePolicyId: SETTLEMENT-01
documentType: GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SETTLEMENT

판매자는 정산 메뉴에서 자신이 등록한 공연들의 정산 목록을 조회할 수 있습니다. 목록에서 공연별 총 결제액, 수수료와 최종 정산액을 확인하며 다른 판매자의 정산은 볼 수 없습니다.

## SETTLEMENT-02 공연별 정산 금액 구성

sourcePolicyId: SETTLEMENT-02
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SETTLEMENT

공연별 정산 금액은 총 결제액, 수수료와 최종 정산액으로 구분됩니다. 최종 정산액은 서비스의 정산 기준에 따라 계산된 값을 사용합니다.

## SETTLEMENT-03 정산 건별 결제 명세

sourcePolicyId: SETTLEMENT-03
documentType: GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SETTLEMENT

정산 건을 선택하면 그 정산에 포함된 개별 결제 명세를 확인할 수 있습니다. 결제와 취소 내역이 정산금액에 어떻게 반영됐는지 건별로 조회할 때 사용합니다.

## SETTLEMENT-04 정산 상세 검색 조건

sourcePolicyId: SETTLEMENT-04
documentType: GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SETTLEMENT

정산 상세 목록은 제공되는 검색 조건과 페이지 조건으로 필요한 결제 명세를 좁혀 볼 수 있습니다. 조건에 맞는 결과가 없으면 빈 목록이 표시되므로 검색 조건을 변경해 다시 조회합니다.

## SETTLEMENT-05 판매자 등급별 수수료율

sourcePolicyId: SETTLEMENT-05
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SETTLEMENT

판매자 등급에 따라 정산에 적용되는 수수료율이 달라질 수 있습니다. 정산 상세에서 적용된 금액을 확인해야 합니다.

## SETTLEMENT-06 삭제된 공연의 정산 표시

sourcePolicyId: SETTLEMENT-06
documentType: FAQ
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SETTLEMENT

정산 대상 공연이 삭제되어도 기존 정산 기록은 유지되며 공연 제목은 삭제된 공연으로 표시될 수 있습니다. 정산 금액과 명세는 해당 기록에서 확인할 수 있습니다.

## SETTLEMENT-07 본인 정산만 조회 가능

sourcePolicyId: SETTLEMENT-07
documentType: POLICY
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SETTLEMENT

판매자는 자신의 계정에 속한 정산만 조회할 수 있습니다. 다른 판매자의 정산을 요청하면 권한 오류가 발생합니다.

## SETTLEMENT-08 취소 결제가 정산에 반영되는 과정

sourcePolicyId: SETTLEMENT-08
documentType: GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SETTLEMENT

예매 결제가 취소되면 취소 정보가 정산 처리로 전달되고 해당 공연의 정산을 다시 계산할 대상으로 표시합니다. 반영 시점 전에는 이전 금액이 보일 수 있으며 재계산 후 취소 금액이 정산에서 제외됩니다.

## SETTLEMENT-09 존재하지 않는 정산 내역

sourcePolicyId: SETTLEMENT-09
documentType: ERROR_GUIDE
audience: SELLER
implementationStatus: IMPLEMENTED
domain: SETTLEMENT

요청한 정산 ID가 없거나 본인에게 속하지 않으면 정산 내역을 조회할 수 없습니다. 판매자는 자신의 정산 목록에서 현재 존재하는 정산 건을 다시 선택해야 합니다.

## ADMIN-01 검수 대기 공연 조회

sourcePolicyId: ADMIN-01
documentType: GUIDE
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: ADMIN

관리자는 공연 관리 화면에서 아직 검수가 끝나지 않은 검수 대기 공연만 모아 조회할 수 있습니다. 목록에서 공연 내용을 확인한 뒤 승인하거나 반려할 대상을 선택합니다.

## ADMIN-02 공연 승인 처리

sourcePolicyId: ADMIN-02
documentType: GUIDE
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: ADMIN

관리자는 검수 대기 공연의 정보가 적절하면 승인 처리할 수 있습니다. 승인된 공연은 게시 가능한 상태가 되어 일반 고객의 공연 목록과 검색에 노출됩니다.

## ADMIN-03 공연 반려와 반려 사유

sourcePolicyId: ADMIN-03
documentType: GUIDE
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: ADMIN

관리자가 공연을 반려할 때는 판매자가 보완할 수 있도록 반려 사유를 반드시 입력해야 합니다. 반려된 공연은 판매자에게 사유가 제공되고 승인되기 전까지 고객에게 게시되지 않습니다.

## ADMIN-04 회원 검색과 페이징

sourcePolicyId: ADMIN-04
documentType: GUIDE
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: ADMIN

관리자는 회원 관리 화면에서 회원을 검색하고 결과를 페이지 단위로 조회할 수 있습니다. 회원이 많으면 검색 조건을 적용하고 페이지를 이동해 나머지 결과를 확인합니다.

## ADMIN-05 회원 정지와 사유 기록

sourcePolicyId: ADMIN-05
documentType: GUIDE
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: ADMIN

관리자는 운영 정책 위반 회원을 정지하면서 정지 사유를 기록할 수 있습니다. 정지된 회원은 해제되기 전까지 로그인이나 회원 기능 이용이 제한될 수 있으며 처리 이력은 관리 목적으로 보관됩니다.

## ADMIN-06 회원 정지 해제

sourcePolicyId: ADMIN-06
documentType: GUIDE
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: ADMIN

관리자는 정지된 회원의 상태와 사유를 확인한 뒤 정지를 해제할 수 있습니다. 해제된 계정은 정상 상태로 돌아가 허용된 회원 기능을 다시 이용할 수 있습니다.

## ADMIN-07 공연장 등록·수정·삭제

sourcePolicyId: ADMIN-07
documentType: GUIDE
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: ADMIN

관리자는 공연장을 새로 등록하고 좌석 행·열 등 공연장 정보를 수정하거나 삭제할 수 있습니다. 회차에서 사용 중인 공연장 상태를 확인한 뒤 관리해야 합니다.

## ADMIN-08 일별 매출 통계와 재집계

sourcePolicyId: ADMIN-08
documentType: GUIDE
audience: ADMIN
implementationStatus: IMPLEMENTED
domain: ADMIN

관리자는 날짜별 매출 통계를 조회하고 집계 결과에 문제가 있으면 해당 일자의 통계를 다시 집계할 수 있습니다. 재집계 결과는 완료된 결제와 취소 내역을 기준으로 갱신됩니다.

## COMMON-01 잘못된 입력값 오류

sourcePolicyId: COMMON-01
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: COMMON

필수값 누락, 형식 오류 또는 허용 범위를 벗어난 값이 있으면 잘못된 입력값 오류가 발생합니다. 오류가 표시된 항목을 고친 뒤 다시 요청해야 합니다.

## COMMON-02 이미 처리된 요청

sourcePolicyId: COMMON-02
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: COMMON

이미 완료되었거나 같은 식별자로 처리된 요청을 다시 보내면 이미 처리된 요청 오류가 발생할 수 있습니다. 현재 결과를 먼저 조회한 뒤 필요한 경우에만 새 요청을 보내야 합니다.

## COMMON-03 서버 오류가 발생한 경우

sourcePolicyId: COMMON-03
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: COMMON

서버 내부 오류가 발생하면 같은 요청을 계속 반복하지 말고 잠시 뒤 다시 시도해야 합니다. 문제가 계속되면 발생 시각과 작업 종류를 고객지원에 전달합니다.

## COMMON-04 존재하지 않는 정보를 조회한 경우

sourcePolicyId: COMMON-04
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: COMMON

삭제되었거나 존재하지 않는 ID를 조회하면 정보를 찾을 수 없다는 오류가 발생합니다. 최신 목록에서 대상을 다시 선택해야 합니다.

## COMMON-05 현재 상태에서 처리할 수 없는 요청

sourcePolicyId: COMMON-05
documentType: ERROR_GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: COMMON

현재 상태에서 허용되지 않는 작업은 처리할 수 없습니다. 이미 취소된 예매를 다시 취소하는 경우처럼 대상의 최신 상태를 확인해야 합니다.

## COMMON-06 목록 조회의 페이지 이동

sourcePolicyId: COMMON-06
documentType: GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: COMMON

목록 화면은 페이지 단위로 결과를 보여주며 페이지 번호와 크기에 따라 다른 구간을 조회합니다. 다음 페이지로 이동해 나머지 결과를 확인할 수 있습니다.

## COMMON-07 검색 결과가 비어 있는 경우

sourcePolicyId: COMMON-07
documentType: GUIDE
audience: COMMON
implementationStatus: IMPLEMENTED
domain: COMMON

조건에 맞는 항목이 없으면 오류 대신 빈 목록이 표시될 수 있습니다. 검색어와 필터를 지우거나 다른 조건으로 다시 조회합니다.

## COMMON-08 로그인 페이지로 다시 이동한 경우

sourcePolicyId: AUTH-02
documentType: FAQ
audience: COMMON
implementationStatus: IMPLEMENTED
domain: COMMON

인증 정보가 만료되거나 접근 권한이 없으면 로그인 페이지로 이동할 수 있습니다. 다시 로그인한 뒤에도 반복되면 계정 역할과 접근하려는 기능을 확인해야 합니다.

## FAQ-AUTH-01 자꾸 로그인이 풀리는 이유

sourcePolicyId: AUTH-02
documentType: FAQ
audience: COMMON
implementationStatus: IMPLEMENTED
domain: FAQ

사용 중 로그인이 자꾸 풀리면 Access Token이 만료되었고 Refresh Token으로 갱신하지 못한 경우일 수 있습니다. 토큰 갱신이 불가능하면 로그인 화면에서 다시 로그인해야 합니다.

## FAQ-AUTH-02 로그아웃했는데 뒤로 가면 화면이 보이는 경우

sourcePolicyId: AUTH-04
documentType: FAQ
audience: COMMON
implementationStatus: IMPLEMENTED
domain: FAQ

로그아웃 직후 뒤로 가기로 이전 화면이 브라우저에 잠시 보일 수 있지만 서버의 Refresh Token은 제거됩니다. 회원 전용 데이터를 다시 요청하면 인증이 거부되므로 공용 기기에서는 창도 닫아야 합니다.

## FAQ-AUTH-03 로그인했는데 판매자 화면에 못 들어가는 이유

sourcePolicyId: AUTH-07
documentType: FAQ
audience: COMMON
implementationStatus: IMPLEMENTED
domain: FAQ

로그인했더라도 일반 회원 계정은 판매자 전용 화면에 들어갈 수 없습니다. 공연 등록과 정산 기능을 사용하려면 판매자 권한이 있는 계정으로 로그인해야 합니다.

## FAQ-MEMBER-01 같은 이메일로 다시 가입할 수 없는 이유

sourcePolicyId: MEMBER-03
documentType: FAQ
audience: COMMON
implementationStatus: IMPLEMENTED
domain: FAQ

이미 가입에 사용된 이메일은 새 회원 가입에 다시 사용할 수 없습니다. 기존 계정으로 로그인하거나 사용되지 않은 다른 이메일로 가입해야 합니다.

## FAQ-MEMBER-02 닉네임 변경 중 중복 오류가 나는 경우

sourcePolicyId: MEMBER-04
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

닉네임을 변경할 때 다른 회원이 이미 사용하는 닉네임을 입력하면 중복 오류가 발생합니다. 중복되지 않는 새 닉네임을 입력해야 변경할 수 있습니다.

## FAQ-MEMBER-03 탈퇴한 계정으로 다시 로그인할 수 없는 이유

sourcePolicyId: MEMBER-08
documentType: FAQ
audience: COMMON
implementationStatus: IMPLEMENTED
domain: FAQ

탈퇴 처리가 완료된 계정은 정상 회원 상태가 아니므로 기존 이메일과 비밀번호로 로그인할 수 없습니다. 탈퇴 전 필요한 예매나 정산 정보를 확인해야 합니다.

## FAQ-EVENT-01 검색한 공연이 목록에 안 보이는 이유

sourcePolicyId: EVENT-10
documentType: FAQ
audience: COMMON
implementationStatus: IMPLEMENTED
domain: FAQ

공연 제목을 검색했는데 나오지 않는다면 공연이 아직 검수 대기·반려 상태이거나 판매가 종료되었을 수 있습니다. 일반 고객 검색에는 관리자가 승인해 게시 중인 공연만 노출됩니다.

## FAQ-EVENT-02 지난 공연을 어디서 보는지

sourcePolicyId: EVENT-13
documentType: FAQ
audience: COMMON
implementationStatus: IMPLEMENTED
domain: FAQ

이미 종료된 공연은 현재 판매 중인 공연 목록이 아니라 지난 공연 메뉴에서 확인할 수 있습니다. 종료된 공연에는 새로운 예매를 진행할 수 없습니다.

## FAQ-EVENT-03 공연 관람등급은 어디서 확인하는지

sourcePolicyId: EVENT-04
documentType: FAQ
audience: COMMON
implementationStatus: IMPLEMENTED
domain: FAQ

공연의 전체 관람가, 12세, 15세, 18세 이상 같은 관람등급은 공연 상세 화면에서 확인할 수 있습니다. 예매 전에 관람자의 나이가 해당 등급을 충족하는지 확인해야 합니다.

## FAQ-VENUE-01 같은 공연인데 날짜마다 공연장이 다른 경우

sourcePolicyId: VENUE-04
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

같은 공연이라도 회차마다 공연장과 시작 시간이 따로 지정될 수 있습니다. 날짜를 바꾸어 예매할 때는 선택한 회차의 장소와 시간을 다시 확인해야 합니다.

## FAQ-VENUE-02 같은 좌석 번호인데 가격이 다른 이유

sourcePolicyId: VENUE-06
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

좌석 가격은 좌석 번호만으로 고정되지 않고 공연 회차와 좌석 등급을 기준으로 정해집니다. 같은 번호의 좌석이라도 다른 공연이나 회차에서는 등급과 가격이 달라질 수 있습니다.

## FAQ-QUEUE-01 친구와 같이 눌렀는데 순번이 다른 이유

sourcePolicyId: QUEUE-02
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

친구와 거의 동시에 예매 버튼을 눌러도 서버에 대기열 등록이 완료된 순서가 다르면 서로 다른 순번을 받습니다. 화면을 누른 시각만으로 같은 순번이나 앞 순번이 보장되지 않습니다.

## FAQ-QUEUE-02 대기 화면을 계속 켜둬야 하는지

sourcePolicyId: QUEUE-03
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

대기 화면은 상태를 3초마다 확인하며 차례가 되면 좌석 선택 화면으로 자동 이동합니다. 일시적인 조회 실패는 다음 갱신에서 다시 확인하지만 장시간 화면을 닫으면 이동 안내를 놓칠 수 있습니다.

## FAQ-QUEUE-03 줄을 통과한 뒤 잠시 자리를 비워도 되는지

sourcePolicyId: QUEUE-04
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

대기열을 통과한 입장 권한은 최대 10분 동안 유지됩니다. 잠시 자리를 비울 수는 있지만 10분 안에 예매를 시작하지 않으면 권한이 만료되어 다시 대기해야 할 수 있습니다.

## FAQ-QUEUE-04 새로고침하면 순번이 빨라지는지

sourcePolicyId: QUEUE-05
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

대기 화면을 새로고침하거나 다시 열어도 같은 회원에게 더 빠른 새 순번을 발급하지 않습니다. 대기 중이면 기존 순번을 유지하고 입장 권한이 만료된 경우에만 새 대기 순번이 생길 수 있습니다.

## FAQ-SEAT-01 좌석을 눌렀는데 다른 사람이 가져간 이유

sourcePolicyId: SEAT-01
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

좌석 화면에서 자리를 클릭한 것만으로는 임시 선점되지 않습니다. 예매 요청이 성공하기 전에 다른 회원이 먼저 선점하면 그 좌석을 사용할 수 없으므로 최신 상태에서 다른 자리를 선택해야 합니다.

## FAQ-SEAT-02 자리를 잡고 결제를 미루면 어떻게 되는지

sourcePolicyId: SEAT-02
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

예매 요청이 성공해 잡은 좌석은 7분 동안만 임시로 보호됩니다. 그 안에 결제를 완료하지 않으면 좌석이 자동으로 풀려 다른 회원이 예매할 수 있습니다.

## FAQ-SEAT-03 먼저 클릭했는데 선점에 실패한 이유

sourcePolicyId: SEAT-03
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

좌석은 화면에서 먼저 클릭한 사람이 아니라 예매 요청으로 임시 선점에 먼저 성공한 회원에게 배정됩니다. 동시에 요청하면 한 명만 성공하고 나머지는 다른 좌석을 선택해야 합니다.

## FAQ-SEAT-04 결제 화면에서 자리가 사라진 이유

sourcePolicyId: SEAT-04
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

결제 화면을 오래 열어 둔 사이 좌석 선점 7분이 끝나면 화면이 남아 있어도 해당 좌석으로 결제할 수 없습니다. 좌석 선택부터 새로 진행해야 합니다.

## FAQ-RES-01 예매됐다고 떴는데 결제 완료가 아닌 이유

sourcePolicyId: RESERVATION-01
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

예매 요청 성공은 좌석을 임시 선점하고 결제를 기다리는 상태이지 최종 구매 완료가 아닙니다. 선점 시간 안에 결제까지 완료되어야 예매가 확정됩니다.

## FAQ-RES-02 네 명이 한 번에 예매할 수 없는 이유

sourcePolicyId: RESERVATION-02
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

한 번의 예매에서 선택할 수 있는 좌석은 등급과 관계없이 최대 3개입니다. 네 자리 이상이 필요하면 예매를 나누어야 하며 각 예매의 좌석 확보는 별도로 결정됩니다.

## FAQ-RES-03 공연이 시작된 뒤 빈 좌석을 살 수 없는 이유

sourcePolicyId: RESERVATION-03
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

공연 회차의 시작 시간이 지나면 빈 좌석이 남아 있어도 새로운 예매를 만들 수 없습니다. 화면에 좌석이 보이더라도 회차 시작 시각을 기준으로 요청이 거부됩니다.

## FAQ-PAY-01 카드 오류 후 같은 예매로 다시 결제하는 방법

sourcePolicyId: PAYMENT-01
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

카드 오류로 결제에 실패해도 좌석 선점 7분이 남아 있다면 같은 예매로 다시 결제할 수 있습니다. 선점이 끝났다면 재결제할 수 없으므로 좌석 선택부터 다시 시작해야 합니다.

## FAQ-PAY-02 결제 버튼을 여러 번 누른 경우

sourcePolicyId: PAYMENT-02
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

같은 예매의 결제 버튼을 여러 번 눌러도 이미 완료된 예매에 새 결제를 중복으로 만들지 않습니다. 처리 중에는 결과를 먼저 확인하고 반복해서 요청하지 않는 것이 좋습니다.

## FAQ-REFUND-01 여러 좌석 중 한 자리만 취소할 수 없는 이유

sourcePolicyId: REFUND-08
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

여러 좌석을 한 번에 예매했다면 현재는 그중 한 자리만 골라 취소할 수 없습니다. 예매 전체를 취소한 뒤 필요한 좌석을 새로 예매해야 하며 기존 좌석의 재확보는 보장되지 않습니다.

## FAQ-REFUND-02 취소 완료를 되돌릴 수 없는 이유

sourcePolicyId: REFUND-09
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

취소가 완료된 예매는 결제 완료 상태로 되돌릴 수 없습니다. 다시 관람하려면 남은 좌석으로 새 예매를 해야 하며 이전 좌석이 남아 있다는 보장은 없습니다.

## FAQ-COUPON-01 쿠폰 받기를 눌렀는데 이미 받았다고 나오는 경우

sourcePolicyId: COUPON-03
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

한 회원은 같은 선착순 쿠폰을 한 번만 발급받을 수 있습니다. 이미 발급된 쿠폰을 다시 요청하면 중복 발급 오류가 발생하므로 내 쿠폰 목록에서 보유 여부를 확인해야 합니다.

## FAQ-COUPON-02 쿠폰이 순식간에 품절된 이유

sourcePolicyId: COUPON-04
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

선착순 쿠폰은 여러 사용자가 동시에 요청하므로 화면에 수량이 보이더라도 처리 순서에 따라 빠르게 소진될 수 있습니다. 준비된 수량이 모두 발급되면 더 이상 받을 수 없습니다.

## FAQ-REVIEW-01 공연을 봤는데 후기를 작성할 수 없는 경우

sourcePolicyId: REVIEW-01
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

후기는 해당 공연을 실제로 예매한 회원만 작성할 수 있습니다. 공연을 관람했더라도 로그인한 계정에서 예매 내역이 확인되지 않으면 후기를 등록할 수 없습니다.

## FAQ-REVIEW-02 같은 공연 후기를 또 쓸 수 없는 이유

sourcePolicyId: REVIEW-02
documentType: FAQ
audience: CUSTOMER
implementationStatus: IMPLEMENTED
domain: FAQ

한 회원은 같은 공연에 후기를 한 번만 작성할 수 있습니다. 이미 후기를 작성했다면 두 번째 후기는 중복으로 등록되지 않습니다.

## FAQ-SELLER-01 등록한 공연이 바로 공개되지 않는 이유

sourcePolicyId: EVENT-09
documentType: FAQ
audience: SELLER
implementationStatus: IMPLEMENTED
domain: FAQ

새로 등록한 공연은 바로 공개되지 않고 먼저 관리자 검수를 기다립니다. 관리자가 승인해야 고객의 공연 목록과 검색 결과에 표시됩니다.

## FAQ-SELLER-02 공연 수정 권한이 없다고 나오는 이유

sourcePolicyId: SELLER-07
documentType: FAQ
audience: SELLER
implementationStatus: IMPLEMENTED
domain: FAQ

공연 수정 권한 오류가 발생했다면 로그인한 판매자가 그 공연을 직접 등록한 소유자인지 확인해야 합니다. 판매자 계정이어도 다른 판매자의 공연은 수정할 수 없습니다.

## FAQ-SELLER-03 포스터 업로드가 거절되는 이유

sourcePolicyId: SELLER-11
documentType: FAQ
audience: SELLER
implementationStatus: IMPLEMENTED
domain: FAQ

포스터가 거절되면 파일이 이미지 형식인지와 크기가 5MB 이하인지 확인해야 합니다. 조건이 맞는데도 실패가 반복되면 파일 저장 또는 서버 오류일 수 있습니다.

## FAQ-SETTLEMENT-01 결제금액과 입금 예정액이 다른 이유

sourcePolicyId: SETTLEMENT-02
documentType: FAQ
audience: SELLER
implementationStatus: IMPLEMENTED
domain: FAQ

고객의 총 결제금액에서 판매자 등급에 따른 수수료가 반영되므로 실제 입금 예정 정산액은 결제금액과 다를 수 있습니다. 정산 상세에서 총액, 수수료와 최종 정산액을 각각 확인해야 합니다.

## FAQ-SETTLEMENT-02 취소된 결제가 정산에서 빠지는 시점

sourcePolicyId: SETTLEMENT-08
documentType: FAQ
audience: SELLER
implementationStatus: IMPLEMENTED
domain: FAQ

취소된 결제는 취소 정보가 정산 처리에 전달되고 해당 공연 정산이 다시 계산된 뒤 제외됩니다. 처리 중에는 이전 금액이 잠시 보일 수 있으므로 재집계 후 정산 상세를 확인해야 합니다.




