package com.ticketon.ai.policy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.policy-ingestion.enabled", havingValue = "true")
public class PolicyIngestionRunner implements ApplicationRunner {

    private final PolicyIngestionService ingestionService;
    private final Path policyDirectory;

    public PolicyIngestionRunner(
            PolicyIngestionService ingestionService,
            @Value("${app.policy-ingestion.directory:policies}") String policyDirectory
    ) {
        this.ingestionService = ingestionService;
        this.policyDirectory = Path.of(policyDirectory);
    }

    @Override
    public void run(ApplicationArguments args) {
        int ingestedCount = ingestionService.ingest(policyDirectory);

        log.info("TicketOn 고객지원 정책 적재 완료: {}개", ingestedCount);
    }
}
