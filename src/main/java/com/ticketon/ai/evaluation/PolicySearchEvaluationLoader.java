package com.ticketon.ai.evaluation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@Component
public class PolicySearchEvaluationLoader {

    private final ObjectMapper objectMapper;
    private final String evaluationFile;

    public PolicySearchEvaluationLoader(
            ObjectMapper objectMapper,
            @Value("${app.policy-evaluation.dataset:development}") String dataset
    ) {
        this.objectMapper = objectMapper;
        this.evaluationFile = resolveEvaluationFile(dataset);
    }

    public List<PolicySearchEvaluationCase> load() {
        ClassPathResource resource = new ClassPathResource(evaluationFile);

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<PolicySearchEvaluationCase>>() {
                    }
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "정책 검색 평가 파일을 읽을 수 없습니다: " + evaluationFile,
                    e
            );
        }
    }

    private String resolveEvaluationFile(String dataset) {
        return switch (dataset.toLowerCase(Locale.ROOT)) {
            case "development" -> "evaluation/policy-search-development.json";
            case "challenging-development" ->
                    "evaluation/policy-search-challenging-development.json";
            case "holdout" -> "evaluation/policy-search-holdout.json";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 정책 검색 평가 데이터셋입니다: " + dataset
            );
        };
    }
}
