package abn.parking.core.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Entity class for parking sessions.
 * <p>
 * Represents a session that is started and ended by the driver of a vehicle when they park on a specific street.
 */
@Data
@Entity(name = "parking_session")
public class ParkingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The license plate of the vehicle that is parked.
     */
    @Column(name = "license", nullable = false)
    private String license;

    /**
     * The street where the vehicle is parked.
     */
    @Column(name = "street", nullable = false)
    private String street;

    /**
     * The time when the driver started the parking session.
     */
    @Column(name = "start_instant", nullable = false)
    private Instant startInstant;

    /**
     * The time when the driver ended the parking session.
     */
    @Column(name = "end_instant")
    private Instant endInstant;
}
