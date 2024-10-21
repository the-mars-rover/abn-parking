package abn.parking.core.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "observations.process")
@Getter
@Setter
public class ObservationsProcessProperties {
    /**
     * The cron expression which defines the schedule for the fine task to run.
     */
    String cron = "0 0 0 * * *";
}
