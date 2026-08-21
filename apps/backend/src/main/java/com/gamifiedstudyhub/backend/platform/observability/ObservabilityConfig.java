package com.gamifiedstudyhub.backend.platform.observability;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enables the {@link io.micrometer.observation.annotation.Observed @Observed}
 * annotation on beans/methods. Each observation becomes both a Micrometer timer
 * (scraped at {@code /actuator/prometheus}) and, when tracing is sampled, a span
 * exported over OTLP — one annotation, metrics + traces together.
 *
 * <p>Requires AOP (spring-boot-starter-aop). The {@code ObservationRegistry} is
 * auto-configured by Spring Boot.
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
