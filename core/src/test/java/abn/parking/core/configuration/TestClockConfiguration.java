package abn.parking.core.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class TestClockConfiguration {
    @Bean
    @Primary
    public Clock clock() {
        // For testing purposes, we always want to use the same time (Saturday, 6 January 2024, 21:00:00 UTC)
        return Clock.fixed(Instant.parse("2024-01-06T21:00:00Z"), ZoneId.of("UTC"));
    }
}
