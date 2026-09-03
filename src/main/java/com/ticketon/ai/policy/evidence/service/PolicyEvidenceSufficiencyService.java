package com.ticketon.ai.policy.evidence.service;

import com.ticketon.ai.observation.AiStageObservation;
import com.ticketon.ai.policy.context.dto.PolicyContext;
import com.ticketon.ai.policy.evidence.domain.PolicyEvidenceSufficiency;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyEvidenceSufficiencyService {

    private static final String SYSTEM_PROMPT = """
            사용자의 질문에 답하는 데 제공된 정책 Context가 충분한지만 판정하세요.

            판정 규칙:
            - 질문의 핵심 내용을 Context가 직접 설명하면 sufficient=true입니다.
            - 질문과 관련된 단어만 있고 필요한 정책 내용이 없으면 sufficient=false입니다.
            - Context에 없는 사실을 추측해야 답할 수 있으면 sufficient=false입니다.
            - 사용자가 요구한 여러 내용 중 일부만 확인할 수 있어도 sufficient=false입니다.
            - 질문에 답하거나 새로운 정책을 만들지 마세요.
            - 설명 없이 JSON 객체 하나만 출력하세요.

            출력 형식:
            {
              "sufficient": true
            }
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final PolicyEvidenceOutputParser outputParser;
    private final AiStageObservation aiStageObservation;

    public PolicyEvidenceSufficiency evaluate(
            String question,
            PolicyContext context
    ) {
        return aiStageObservation.observe(
                "evidence-gate",
                () -> evaluateEvidence(question, context)
        );
    }

    private PolicyEvidenceSufficiency evaluateEvidence(
            String question,
            PolicyContext context
    ) {
        String output = chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user("""
                        [사용자 질문]
                        %s

                        [정책 Context]
                        %s
                        """.formatted(question, context.content()))
                .options(OllamaChatOptions.builder()
                        .disableThinking()
                        .temperature(0.0)
                        .format("json"))
                .call()
                .content();

        return outputParser.parse(output);
    }
}
