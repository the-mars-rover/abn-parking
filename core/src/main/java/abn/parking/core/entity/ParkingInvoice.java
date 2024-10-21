package abn.parking.core.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Entity class for parking invoices.
 * <p>
 * Represents a parking invoice which is created when a parking session is closed or when a vehicle has been observed
 * as parked on a street but did not have an open parking session for the time at which it was observed.
 */
@Data
@Entity(name = "parking_invoice")
public class ParkingInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * The parking session for which the parking invoice was created (if the invoice was created based on a parking session).
     */
    @OneToOne
    @JoinColumn(name = "session_id")
    private ParkingSession session;

    /**
     * The observation for which the parking invoice was created (if the invoice was created based on an observation).
     */
    @OneToOne
    @JoinColumn(name = "observation_id")
    private VehicleObservation observation;

    /**
     * The date and time at which the parking invoice was created.
     */
    @Column(name = "invoice_instant", nullable = false)
    private Instant invoiceInstant;

    /**
     * The amount of the parking invoice (cents), which is calculated based on the parking rate (see {@link ParkingRate})
     * of the street for a parking session or vehicle observation (if a driver did not start a parking session).
     */
    @Column(name = "amount", nullable = false)
    private Long amount;

    /**
     * Whether or not the fine has been paid.
     */
    @Column(name = "paid", nullable = false)
    private Boolean paid;
}
