package com.ticketon.ai.evaluation.generation;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GenerationEvaluationLoader {

    private final ObjectMapper objectMapper;
    private final Environment environment;

    public List<GenerationEvaluationCase> load() {
        String dataset = environment.getProperty(
                "app.generation-evaluation.dataset",
                "development"
        );
        String evaluationFile = resolveEvaluationFile(dataset);
        ClassPathResource resource = new ClassPathResource(evaluationFile);

        try (InputStream inputStream = resource.getInputStream()) {
            List<GenerationEvaluationCase> evaluationCases = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {
                    }
            );

            validate(evaluationCases);
            return evaluationCases;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Generation 평가 파일을 읽을 수 없습니다: " + evaluationFile,
                    e
            );
        }
    }

    private void validate(List<GenerationEvaluationCase> evaluationCases) {
        Set<String> ids = new HashSet<>();

        for (GenerationEvaluationCase evaluationCase : evaluationCases) {
            validateRequiredFields(evaluationCase);

            if (!ids.add(evaluationCase.id())) {
                throw new IllegalStateException(
                        "Generation 평가 문항 ID가 중복됐습니다: " + evaluationCase.id()
                );
            }
        }
    }

    private void validateRequiredFields(GenerationEvaluationCase evaluationCase) {
        if (isBlank(evaluationCase.id())
                || isBlank(evaluationCase.category())
                || isBlank(evaluationCase.question())
                || evaluationCase.expectedPolicyIds() == null
                || evaluationCase.mustIncludeFacts() == null
                || evaluationCase.mustNotIncludeFacts() == null) {
            throw new IllegalStateException(
                    "Generation 평가 문항의 필수값이 누락됐습니다: " + evaluationCase.id()
            );
        }

        if (!evaluationCase.shouldAbstain()
                && evaluationCase.expectedPolicyIds().isEmpty()) {
            throw new IllegalStateException(
                    "답변 가능한 문항에는 정답 정책 ID가 필요합니다: "
                            + evaluationCase.id()
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String resolveEvaluationFile(String dataset) {
        return switch (dataset.toLowerCase(Locale.ROOT)) {
            case "development" -> "evaluation/policy-answer-development.json";
            case "holdout" -> "evaluation/policy-answer-holdout.json";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 Generation 평가 데이터셋입니다: " + dataset
            );
        };
    }
}
