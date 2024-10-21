package abn.parking.core.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity class for parking rates.
 * <p>
 * Represents the rate at which parked vehicles are charged for parking on a street.
 */
@Data
@Entity(name = "parking_rate")
public class ParkingRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The street where vehicles may be parked.
     */
    @Column(name = "street", nullable = false, unique = true)
    private String street;

    /**
     * The price (in cents per minute) at which vehicles are charged for parking on the street.
     */
    @Column(name = "rate", nullable = false)
    private Integer rate;

    /**
     * The flat price (in cents) which vehicles are charged when they have parked on the street without an open parking session.
     */
    @Column(name = "fine_rate", nullable = false)
    private Integer fineRate;
}
