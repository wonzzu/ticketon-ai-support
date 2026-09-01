package com.ticketon.ai.policy.search.controller;

import com.ticketon.ai.policy.search.dto.PolicySearchResponse;
import com.ticketon.ai.policy.search.service.PolicyRetrievalService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/policies")
public class PolicySearchController {

    private final PolicyRetrievalService policyRetrievalService;

    @GetMapping("/search")
    public List<PolicySearchResponse> search(@RequestParam @NotBlank String query) {
        return policyRetrievalService.retrieve(query);
    }
}
