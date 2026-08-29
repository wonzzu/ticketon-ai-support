# TicketOn AI Support

TicketOn의 취소·환불 문의에 정책 근거와 사용자별 예매 정보를 결합해 답변하는 AI 고객지원 서비스입니다.

정책 지식은 RAG로 검색하고, 사용자별 실시간 정보는 Tool로 조회하며, 정책 적용과 환불액 계산은 Java의 결정적 로직이 담당하는 구조를 목표로 합니다.

## 기술 스택

- Java 21
- Spring Boot 4.1.1
- Gradle

## 진행 상태

- [x] 별도 Spring Boot 서비스 구성
- [x] Java 21 기본 실행 및 테스트
- [ ] Ollama Chat·Embedding Model 연동
- [ ] PostgreSQL·pgvector 기반 정책 검색
- [ ] 평가셋 기반 RAG 품질 측정
- [ ] TicketOn 예매 조회·환불 계산 Tool 연동

상세 설계와 평가 결과는 구현 및 검증이 완료되는 순서대로 추가합니다.
