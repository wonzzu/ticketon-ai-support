package com.ticketon.ai.policy.answer.service;

import com.ticketon.ai.policy.answer.domain.PolicyAnswerModelOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PolicyAnswerModelOutputParser {

    private final ObjectMapper objectMapper;

    public PolicyAnswerModelOutput parse(String output) {
        try {
            return objectMapper.readValue(
                    removeCodeFence(output),
                    PolicyAnswerModelOutput.class
            );
        } catch (Exception exception) {
            return null;
        }
    }

    private String removeCodeFence(String output) {
        if (output == null) {
            return "";
        }

        return output.strip()
                .replaceFirst("^```(?:json)?\\s*", "")
                .replaceFirst("\\s*```$", "");
    }
}
