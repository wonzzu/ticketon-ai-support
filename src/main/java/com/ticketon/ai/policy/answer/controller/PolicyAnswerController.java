package com.ticketon.ai.policy.answer.controller;

import com.ticketon.ai.policy.answer.dto.PolicyAnswerRequest;
import com.ticketon.ai.policy.answer.dto.PolicyAnswerResponse;
import com.ticketon.ai.policy.answer.service.PolicyAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/policy-answers")
public class PolicyAnswerController {

    private final PolicyAnswerService policyAnswerService;

    @PostMapping
    public PolicyAnswerResponse answer(@Valid @RequestBody PolicyAnswerRequest request) {
        return policyAnswerService.answer(request.question());
    }
}