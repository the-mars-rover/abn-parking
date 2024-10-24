package abn.parking.core.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfiguration {
    @Bean
    public Clock testClock() {
        return Clock.systemDefaultZone();
    }
}
