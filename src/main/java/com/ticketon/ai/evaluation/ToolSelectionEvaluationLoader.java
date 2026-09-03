package com.ticketon.ai.evaluation;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class ToolSelectionEvaluationLoader {

    private static final String EVALUATION_FILE =
            "evaluation/tool-selection-evaluation.json";

    private final ObjectMapper objectMapper;

    public ToolSelectionEvaluationLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ToolSelectionEvaluationCase> load() {
        ClassPathResource resource = new ClassPathResource(EVALUATION_FILE);

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<ToolSelectionEvaluationCase>>() {
                    }
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Tool 선택 평가 파일을 읽을 수 없습니다: " + EVALUATION_FILE,
                    e
            );
        }
    }
}
