package abn.parking.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Entity class for vehicle observations.
 * <p>
 * A vehicle observation is an observation (by a specialized car equipped with a camera) of a vehicle being parked on
 * a street at a specific time. All vehicle observations need to be verified to check if the vehicle had a valid parking
 * session at the time of observation. If the vehicle did not have a valid parking session, a parking fine will be
 * created for the vehicle.
 */
@Data
@Entity(name = "vehicle_observation")
public class VehicleObservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The license plate of the parked vehicle.
     */
    @Column(name = "license", nullable = false)
    private String license;

    /**
     * The street where the vehicle is parked.
     */
    @Column(name = "street", nullable = false)
    private String street;

    /**
     * The time when the vehicle with the above license plate was observed as parked on the street.
     */
    @Column(name = "observation_instant", nullable = false)
    private Instant observationInstant;

    /**
     * The time when it was verified that the vehicle with the above license plate had a valid parking session
     * for parking on the street at the time of observation. Even if the vehicle did not have a valid parking session,
     * this field will be set to true when a parking invoice (fine) has been created for the vehicle.
     */
    @Column(name = "verified", nullable = false)
    private Boolean verified;
}
