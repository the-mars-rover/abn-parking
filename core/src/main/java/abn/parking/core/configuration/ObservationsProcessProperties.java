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
     * The interval (in minutes) which defines how often the fine task to run. Defaults to 5.
     */
    int interval = 1;
}
