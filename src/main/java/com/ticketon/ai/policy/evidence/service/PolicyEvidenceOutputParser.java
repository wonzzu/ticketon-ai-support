package com.ticketon.ai.policy.evidence.service;

import com.ticketon.ai.policy.evidence.domain.PolicyEvidenceModelOutput;
import com.ticketon.ai.policy.evidence.domain.PolicyEvidenceSufficiency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PolicyEvidenceOutputParser {

    private final ObjectMapper objectMapper;

    public PolicyEvidenceSufficiency parse(String output) {
        try {
            PolicyEvidenceModelOutput modelOutput = objectMapper.readValue(
                    removeCodeFence(output),
                    PolicyEvidenceModelOutput.class
            );

            if (modelOutput.sufficient() == null) {
                return PolicyEvidenceSufficiency.outputIssue();
            }

            return PolicyEvidenceSufficiency.sufficient(
                    modelOutput.sufficient()
            );
        } catch (Exception exception) {
            return PolicyEvidenceSufficiency.outputIssue();
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
