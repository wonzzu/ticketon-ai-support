package com.ticketon.ai.policy.evidence.domain;

public record PolicyEvidenceSufficiency(
        boolean sufficient,
        boolean structuredOutputPass,
        String failureReason
) {

    public static PolicyEvidenceSufficiency sufficient(boolean sufficient) {
        return new PolicyEvidenceSufficiency(sufficient, true, "");
    }

    public static PolicyEvidenceSufficiency outputIssue() {
        return new PolicyEvidenceSufficiency(
                false,
                false,
                "Evidence Sufficiency 출력 JSON을 해석할 수 없습니다."
        );
    }
}
