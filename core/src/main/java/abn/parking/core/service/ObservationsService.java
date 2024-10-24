package abn.parking.core.service;

import abn.parking.core.dto.AddObservationsRequest;
import abn.parking.core.entity.ParkingInvoice;
import abn.parking.core.entity.ParkingRate;
import abn.parking.core.entity.VehicleObservation;
import abn.parking.core.repository.ParkingInvoiceRepository;
import abn.parking.core.repository.ParkingRateRepository;
import abn.parking.core.repository.ParkingSessionRepository;
import abn.parking.core.repository.VehicleObservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObservationsService {
    private final Clock clock;
    private final VehicleObservationRepository vehicleObservationRepository;
    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingRateRepository parkingRateRepository;
    private final ParkingInvoiceRepository parkingInvoiceRepository;

    public void addObservations(AddObservationsRequest addObservationsRequest) {
        // map observations to entity objects
        var vehicleObservations = addObservationsRequest.getObservations().stream()
                .map(observation -> {
                    var vehicleObservation = new VehicleObservation();
                    vehicleObservation.setLicense(observation.getLicense());
                    vehicleObservation.setObservationInstant(observation.getObservationInstant());
                    vehicleObservation.setStreet(observation.getStreet());
                    vehicleObservation.setVerified(false);
                    return vehicleObservation;
                }).toList();

        // save observations (if the list gets too large, consider using a batch insert)
        vehicleObservationRepository.saveAll(vehicleObservations);
    }

    @Transactional
    @Scheduled(fixedDelayString = "${observations.process.interval}", timeUnit = TimeUnit.MINUTES)
    public void verifyObservations() {
        log.info("Verifying observations | State = Finished");

        // Find all unverified observations that
        var unverifiedObservations = vehicleObservationRepository.findAllByVerifiedIsFalse();

        unverifiedObservations.forEach(observation -> {
            // find session
            var optionalSession = parkingSessionRepository.findSessionForObservation(observation);

            // if no session is found, fine the vehicle
            if (optionalSession.isEmpty()) {
                createInvoice(observation);
            }

            // mark the observation as verified
            observation.setVerified(true);
            vehicleObservationRepository.save(observation);
        });

        log.info("Verifying observations  | State = Finished");
    }

    private void createInvoice(VehicleObservation observation) {
        var rate = parkingRateRepository.findByStreet(observation.getStreet()).map(ParkingRate::getFineRate)
                .orElse(0); // if no fine rate was found for the given street, the rate is 0

        if (rate == 0) {
            return;
        }

        var invoice = new ParkingInvoice();
        invoice.setObservation(observation);
        invoice.setInvoiceInstant(Instant.now(clock));
        invoice.setAmount(rate.longValue());
        invoice.setPaid(false);
        parkingInvoiceRepository.save(invoice);
    }
}
