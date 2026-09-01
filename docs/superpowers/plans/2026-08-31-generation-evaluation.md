# Generation Evaluation Implementation Plan

> **For agentic workers:** Steps use checkbox (`- [ ]`) syntax for tracking. Execute inline in the current workspace unless the user explicitly requests subagents.

**Goal:** qwen3:8b가 생성한 TicketOn 정책 답변을 Java 규칙과 llama3.1:8b 채점관으로 평가하고 Retrieval, Generation, E2E 결과를 분리한다.

**Architecture:** Development 질문을 qwen3:8b로 먼저 모두 생성해 메모리에 보관한 뒤 llama3.1:8b로 일괄 채점한다. Judge는 평가 실행에서만 생성되며 운영 API에는 연결하지 않는다.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring AI 2.0.1, Ollama, Jackson, Lombok

**Spec:** `docs/superpowers/specs/2026-08-31-generation-evaluation-design.md`

## Global Constraints

- Development 40문항과 Holdout 15문항을 분리한다.
- Holdout은 Generation 구조 동결 전 실행하지 않는다.
- 답변 생성 모델은 `qwen3:8b`, 채점 모델은 `llama3.1:8b`다.
- Judge를 실제 고객 요청 경로에 연결하지 않는다.
- 평가 실행은 기본적으로 비활성화한다.
- 문서 데이터 작성에 TDD를 적용하지 않는다.
- 사용자 요청 없이 Git branch, commit, push를 실행하지 않는다.

---

### Task 1: Generation 평가 데이터

**Files:**
- Create: `src/main/resources/evaluation/policy-answer-development.json`
- Create: `src/main/resources/evaluation/policy-answer-holdout.json`

**Produces:** `question`, `expectedPolicyIds`, `mustIncludeFacts`, `mustNotIncludeFacts`, `shouldAbstain` 필드를 가진 평가 문항

- [x] 정책 영역과 질문 유형이 한쪽에 쏠리지 않도록 Development 40문항을 작성한다.
- [x] 단일 정책 8, 복합 정책 10, 경계조건 8, 답변 불가 8, 유사 정책 혼동 6문항인지 확인한다.
- [x] 같은 기준으로 Holdout 15문항을 작성하고 실행하지 않은 상태로 봉인한다.
- [x] JSON 문법, ID 중복, 필수 필드, 문항 수만 검사한다.

### Task 2: 평가 데이터 Loader

**Files:**
- Create: `src/main/java/com/ticketon/ai/evaluation/generation/GenerationEvaluationCase.java`
- Create: `src/main/java/com/ticketon/ai/evaluation/generation/GenerationEvaluationLoader.java`
- Create: `src/test/java/com/ticketon/ai/evaluation/generation/GenerationEvaluationLoaderTest.java`

**Produces:** `List<GenerationEvaluationCase> load()`

- [x] `GenerationEvaluationCase`를 다음 필드를 가진 record로 작성한다.

```java
public record GenerationEvaluationCase(
        String id,
        String category,
        String question,
        List<String> expectedPolicyIds,
        List<String> mustIncludeFacts,
        List<String> mustNotIncludeFacts,
        boolean shouldAbstain
) {
}
```

- [x] `app.generation-evaluation.dataset` 값으로 development와 holdout 파일을 선택하는 Loader를 작성한다.
- [x] Loader 테스트에서 Development 40문항, Holdout 15문항, ID 중복 없음과 필수 값 존재를 확인한다.
- [x] `GenerationEvaluationLoaderTest`만 실행해 통과 여부를 확인한다.

### Task 3: 생성 결과 수집

**Files:**
- Create: `src/main/java/com/ticketon/ai/evaluation/generation/GeneratedAnswerCase.java`
- Create: `src/main/java/com/ticketon/ai/evaluation/generation/GenerationAnswerBatchService.java`
- Create: `src/main/java/com/ticketon/ai/policy/answer/domain/PolicyAnswerGeneration.java`
- Modify: `src/main/java/com/ticketon/ai/policy/answer/dto/PolicyAnswerResponse.java`
- Modify: `src/main/java/com/ticketon/ai/policy/answer/service/PolicyAnswerService.java`

**Produces:** 질문, 검색 정책, Context, 생성 답변을 보관하는 `GeneratedAnswerCase`

- [x] `PolicyAnswerResponse`가 답변과 출처 정책 ID를 유지하는지 확인한다.
- [x] 운영 API와 평가가 같은 `PolicyAnswerService`의 Generation 흐름을 사용하도록 한다.
- [x] 40문항을 먼저 모두 생성해 `List<GeneratedAnswerCase>`에 보관하고 Judge 호출과 분리한다.

### Task 4: 별도 LLM 채점관

**Files:**
- Create: `src/main/java/com/ticketon/ai/evaluation/generation/GenerationJudgeService.java`
- Create: `src/main/java/com/ticketon/ai/evaluation/generation/GenerationJudgeResult.java`
- Modify: `src/main/resources/application.yml`

**Consumes:** `GeneratedAnswerCase`, `GenerationEvaluationCase`

**Produces:** `factPass`, `contradictionPass`, `groundednessPass`, `completenessPass`, `judgePass`, `failureReason`

- [x] Judge 전용 ChatClient가 `llama3.1:8b` 계열 모델을 사용하도록 구성한다.
- [x] 질문, 정책 Context, 필수 사실, 금지 사실, 생성 답변, 평가 기준을 구분한 Prompt를 작성한다.
- [x] Judge가 설명 없는 구조화 JSON만 반환하도록 요청한다.
- [x] 평가 기준 밖의 사실을 Judge가 새로 만들지 않도록 Prompt로 제한한다.
- [x] Judge 출력 파싱 실패는 PASS로 처리하지 않고 출력 문제로 기록한다.

### Task 5: Java 채점과 결과 집계

**Files:**
- Create: `src/main/java/com/ticketon/ai/evaluation/generation/GenerationEvaluator.java`
- Create: `src/main/java/com/ticketon/ai/evaluation/generation/GenerationEvaluationResult.java`
- Create: `src/test/java/com/ticketon/ai/evaluation/generation/GenerationEvaluatorTest.java`

**Produces:** Retrieval, Generation, E2E 성공 여부와 실패 이유가 포함된 전체 결과

- [x] Java가 정책 ID, 빈 답변, 답변 거부 여부와 Judge 출력 구조를 검사하도록 한다.
- [x] 답변 가능한 문항은 `e2ePass = retrievalPass && generationPass`로 계산한다.
- [x] 답변 불가 문항은 Retrieval을 평가 대상 아님으로 두고 `e2ePass = generationPass`로 계산한다.
- [x] 답변 거부 문항의 Retrieval 제외와 E2E 계산을 핵심 단위 테스트로 확인한다.
- [x] `GenerationEvaluatorTest`를 실행해 통과 여부를 확인한다.

### Task 6: 선택 실행 Runner와 Baseline

**Files:**
- Create: `src/main/java/com/ticketon/ai/evaluation/generation/GenerationEvaluationRunner.java`
- Modify: `src/main/resources/application.yml`

**Produces:** 설정을 켰을 때만 실행되는 Generation 평가와 결과 로그

- [x] `app.generation-evaluation.enabled=true`일 때만 Runner가 실행되도록 한다.
- [x] qwen 생성 40건을 먼저 끝낸 뒤 llama Judge 40건을 실행한다.
- [x] Retrieval, Generation, E2E 성공률과 실패 유형별 개수를 분리해 기록한다.
- [x] 실패 문항에 질문 ID, 검색 정책, 생성 답변, 실패 이유를 기록하되 전체 질문을 평상시 로그에 남기지 않는다.
- [ ] 사용자가 요청하면 Development만 실행해 현재 A/G Baseline을 측정한다.
- [ ] Holdout은 구조 동결 전 실행하지 않는다.
