package abn.parking.core.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "parking.schedule")
@Getter
@Setter
public class ParkingScheduleProperties {

    /**
     * The start time, from when parking will be billed every day. Defaults to 8:00.
     */
    ScheduleTime startTime = new ScheduleTime(8, 0);

    /**
     * The end hour, until when parking will be billed every day. Defaults to 21:00.
     */
    ScheduleTime endTime = new ScheduleTime(21, 0);

    /**
     * The zone id to use for the parking schedule. Defaults to Europe/Amsterdam.
     */
    String zoneId;

    @AllArgsConstructor
    @Getter
    @Setter
    public static class ScheduleTime {
        /**
         * The hour of the day.
         */
        Integer hour;

        /**
         * The minute of the hour.
         */
        Integer minute;
    }
}
