# TicketOn Generation 평가 설계

## 1. 목적

정책 검색 이후 생성된 고객지원 답변이 검색된 정책을 정확하게 설명하는지 평가한다. 검색 실패와 답변 생성 실패를 분리해 어느 단계에서 문제가 발생했는지 확인할 수 있어야 한다.

## 2. 평가 범위

- Development: 40문항
  - 일반 정책 질문 10문항
  - 잘못된 전제 8문항
  - 날짜·숫자·시간 경계조건 8문항
  - 여러 정책이 필요한 복합질문 8문항
  - 정책으로 답할 수 없는 질문 6문항
- Holdout: 15문항
  - Development 튜닝에 사용하지 않는다.
  - Generation 구조를 동결한 뒤 최초 1회 실행한다.

## 3. 평가 데이터

각 문항은 다음 필드만 사용한다.

```json
{
  "id": "GEN-001",
  "category": "MULTI_POLICY",
  "question": "줄 통과 후 9분 지났는데 지금 좌석을 잡으면 선점 시간도 1분만 남나요?",
  "expectedPolicyIds": ["QUEUE-04", "SEAT-02"],
  "mustIncludeFacts": [
    "대기열 통과 후 입장 권한은 10분 동안 유지된다",
    "좌석 선점은 예매 요청 성공 시점부터 7분 동안 새로 시작된다",
    "두 시간은 서로 별도로 계산된다"
  ],
  "mustNotIncludeFacts": [
    "좌석 선점 시간이 1분만 남는다",
    "대기열 통과 시점부터 좌석 선점 시간이 시작된다"
  ],
  "shouldAbstain": false
}
```

- `expectedPolicyIds`: 검색돼야 하는 정책 ID
- `mustIncludeFacts`: 답변이 의미상 반드시 설명해야 하는 사실
- `mustNotIncludeFacts`: 답변에 포함되면 안 되는 잘못된 사실
- `shouldAbstain`: 정책만으로 답할 수 없어 답변을 거부해야 하는지

처음부터 필드를 추가하지 않는다. 40문항 Baseline을 실행한 뒤 실제 채점에 필요한 정보가 부족할 때만 확장한다.

## 4. 채점 구조

### Retrieval

`retrievalPass`는 `expectedPolicyIds`가 검색된 Top-3에 모두 포함됐는지 나타낸다.

정책으로 답할 수 없는 문항은 `expectedPolicyIds`가 비어 있으므로 Retrieval을 평가하지 않는다. 검색기가 유사 정책을 반환하더라도 Generation이 근거 없는 답변을 만들지 않는지가 성공 기준이다.

### Generation

- `factPass`: `mustIncludeFacts`를 의미상 모두 설명했는가
- `contradictionPass`: `mustNotIncludeFacts`에 해당하는 내용을 말하지 않았는가
- `abstentionPass`: `shouldAbstain` 값에 맞게 답변하거나 답변을 거부했는가
- `judgePass`: 정확성, 근거 충실성, 답변 완전성에 대한 AI 채점관의 종합 판단을 통과했는가

```text
generationPass = factPass
                 && contradictionPass
                 && abstentionPass
                 && judgePass
```

### E2E

정책으로 답할 수 있는 문항:

```text
e2ePass = retrievalPass && generationPass
```

정책으로 답할 수 없는 문항:

```text
e2ePass = generationPass
```

## 5. 채점 책임

답변 생성은 `qwen3:8b`, AI 채점은 다른 계열 모델인 `llama3.1:8b`가 담당한다. 같은 모델이 자신의 답변을 채점하면서 생길 수 있는 자기 편향을 줄이기 위한 분리다.

### Java

- 필요한 정책 ID가 검색됐는지 검사한다.
- 답변과 출처가 비어 있지 않은지 검사한다.
- 답변 거부 문항에서 정해진 거부 응답이 생성됐는지 검사한다.
- AI 채점 결과가 정해진 구조인지 검사한다.
- 실행할 때마다 결과가 달라지면 안 되는 항목만 담당한다.

### AI 채점관

AI 채점관은 생성된 답변을 읽고 의미상 정답인지 평가하는 별도 LLM 호출이다.

- 필수 사실을 다른 표현으로라도 모두 설명했는지 평가한다.
- 금지 사실과 같은 의미의 잘못된 내용을 말했는지 평가한다.
- 제공된 정책에 없는 내용을 추가했는지 평가한다.
- 질문의 모든 의도에 답했는지 평가한다.

`mustIncludeFacts`와 `mustNotIncludeFacts`를 단순 문자열 포함 여부로 검사하지 않는다.

### 사람 확인

Java 검사와 AI 채점관이 충돌하거나 최종 결과가 실패인 문항만 사람이 확인한다. 전체 답변을 매번 수동으로 채점하지 않는다.

## 6. 구성 요소

```text
evaluation/generation/
├─ GenerationEvaluationCase
├─ GenerationEvaluationLoader
├─ GenerationEvaluator
├─ GenerationJudgeService
├─ GenerationEvaluationResult
└─ GenerationEvaluationRunner

src/main/resources/evaluation/
├─ policy-answer-development.json
└─ policy-answer-holdout.json
```

- Loader: 선택한 JSON 평가셋을 읽는다.
- Evaluator: 실제 RAG 파이프라인과 채점 순서를 조정한다.
- JudgeService: AI 채점관에게 구조화된 평가 결과를 요청한다.
- Result: Retrieval, Generation, E2E 결과와 실패 이유를 분리해 보관한다.
- Runner: 설정이 활성화됐을 때만 평가를 실행하고 요약을 기록한다.

## 7. 실행 흐름

```text
Development 질문 40개
→ qwen3:8b로 실제 R→A→G 답변을 모두 생성하고 보관
→ 생성 단계 종료
→ llama3.1:8b로 보관된 답변을 모두 채점
→ Java 기본 검사와 AI 채점관 의미 평가 결합
→ Retrieval / Generation / E2E 결과 계산
→ 실패 이유와 전체 성공률 기록
```

8GB GPU에서 두 8B 모델을 문항마다 번갈아 불러오는 비용을 피하기 위해 생성과 채점을 두 단계의 일괄 작업으로 나눈다. Judge는 개발 평가에서만 사용하며 실제 고객 요청 흐름에는 포함하지 않는다.

평가 실행은 기본적으로 비활성화하며 명시적인 설정으로만 실행한다. 실제 Ollama와 PostgreSQL을 사용하는 평가는 단위 테스트와 분리한다.

## 8. 실패 분류

- Retrieval 실패: 필요한 정책이 Top-3에 없음
- 필수 사실 누락: 질문에 필요한 정책 사실 일부를 설명하지 않음
- 정책 모순: 정책과 반대되거나 잘못된 내용을 생성함
- 근거 없는 생성: 제공된 정책에 없는 사실을 추가함
- 복합질문 누락: 여러 의도 중 일부에만 답함
- 답변 거부 실패: 근거 없는 질문에 답하거나, 근거가 있는데 답변을 거부함
- 채점 출력 오류: AI 채점관 결과를 구조적으로 해석할 수 없음

## 9. 성공 기준

첫 실행은 현재 A/G의 Baseline을 측정하는 용도다. 목표 점수를 미리 정해 평가셋에 맞추지 않는다.

- Development 40문항 전체에서 각 실패 유형과 비율을 기록한다.
- 실패 원인을 확인한 뒤 필요한 개선만 적용한다.
- 변경 후 같은 Development 전체를 재실행해 개선과 악화를 비교한다.
- 구조를 동결한 뒤 Holdout 15문항을 최초 1회 실행한다.
- Holdout 결과를 본 뒤 해당 문항에 맞춰 프롬프트를 수정하지 않는다.
