package com.ticketon.ai.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class AiStageObservation {

    private static final String OBSERVATION_NAME = "ticketon.ai.stage";

    private final ObservationRegistry observationRegistry;

    public <T> T observe(String stage, Supplier<T> operation) {
        return Observation.createNotStarted(
                        OBSERVATION_NAME,
                        observationRegistry
                )
                .lowCardinalityKeyValue("stage", stage)
                .observe(operation);
    }
}
