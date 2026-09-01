package com.ticketon.ai.evaluation.generation;

import com.ticketon.ai.policy.answer.service.PolicyAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class GenerationEvaluationResultWriter {

    private static final Path RESULT_DIRECTORY = Path.of(
            "evaluation-results",
            "generation"
    );
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final PolicyAnswerService policyAnswerService;

    public Path write(String runId, GenerationEvaluationResult result) {
        OffsetDateTime executedAt = OffsetDateTime.now();
        GenerationEvaluationRunReport report = createReport(
                runId,
                executedAt,
                result
        );
        Path resultPath = createResultPath(runId, executedAt);

        try {
            Files.createDirectories(RESULT_DIRECTORY);
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(resultPath.toFile(), report);
            return resultPath.toAbsolutePath();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Generation 평가 결과 저장에 실패했습니다: " + resultPath,
                    exception
            );
        }
    }

    private GenerationEvaluationRunReport createReport(
            String runId,
            OffsetDateTime executedAt,
            GenerationEvaluationResult result
    ) {
        return new GenerationEvaluationRunReport(
                runId,
                executedAt.toString(),
                environment.getProperty(
                        "app.generation-evaluation.dataset",
                        "development"
                ),
                environment.getProperty(
                        "spring.ai.ollama.chat.options.model",
                        "qwen3:8b"
                ),
                policyAnswerService.promptVersion(),
                policyAnswerService.systemPrompt(),
                false,
                environment.getProperty(
                        "spring.ai.ollama.chat.options.num-ctx",
                        "ollama-default"
                ),
                "retrieval-v2",
                result
        );
    }

    private Path createResultPath(String runId, OffsetDateTime executedAt) {
        String fileName = "%s-%s.json".formatted(
                executedAt.format(FILE_TIMESTAMP_FORMAT),
                runId
        );
        return RESULT_DIRECTORY.resolve(fileName);
    }
}
