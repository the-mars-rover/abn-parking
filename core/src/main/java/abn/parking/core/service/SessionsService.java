package abn.parking.core.service;

import abn.parking.core.configuration.ParkingScheduleProperties;
import abn.parking.core.dto.*;
import abn.parking.core.entity.ParkingInvoice;
import abn.parking.core.entity.ParkingRate;
import abn.parking.core.entity.ParkingSession;
import abn.parking.core.repository.ParkingInvoiceRepository;
import abn.parking.core.repository.ParkingRateRepository;
import abn.parking.core.repository.ParkingSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
@RequiredArgsConstructor
public class SessionsService {

    private final Clock clock;
    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingRateRepository parkingRateRepository;
    private final ParkingInvoiceRepository parkingInvoiceRepository;
    private final ParkingScheduleProperties parkingScheduleProperties;

    public StartParkingSessionResponse startParkingSession(String license, StartParkingSessionRequest startParkingSessionRequest) {
        // create the session entity
        var session = new ParkingSession();
        session.setLicense(license);
        session.setStreet(startParkingSessionRequest.getStreet());
        session.setStartInstant(Instant.now(clock));

        // save the session
        var savedSession = parkingSessionRepository.save(session);

        // map the saved session to the response and return it
        return StartParkingSessionResponse.builder()
                .license(savedSession.getLicense())
                .street(savedSession.getStreet())
                .startInstant(savedSession.getStartInstant())
                .build();
    }

    @Transactional
    public Invoice stopParkingSession(String license) {
        // get the open session by license (the NoSuchElementException will lead to a 404 response if no session was found)
        var session = parkingSessionRepository.findByLicenseAndEndInstantIsNull(license).orElseThrow();

        // set the end instant for the session and save it
        session.setEndInstant(Instant.now(clock));
        session = parkingSessionRepository.save(session);

        // calculate the amount that needs to be paid for the session
        var amount = calculateAmountForSession(session);

        // if the amount is 0, no invoice is created
        if (amount == 0) {
            return null;
        }

        // create an invoice for the stopped session and save it
        var invoice = new ParkingInvoice();
        invoice.setSession(session);
        invoice.setInvoiceInstant(session.getEndInstant());
        invoice.setPaid(false);
        invoice.setAmount(amount);
        invoice = parkingInvoiceRepository.save(invoice);

        // return response
        return mapStopParkingSessionResponse(invoice, session);
    }

    private Long calculateAmountForSession(ParkingSession session) {
        // get the rate for the street where the session took place
        var rate = parkingRateRepository.findByStreet(session.getStreet()).map(ParkingRate::getRate)
                .orElse(0); // if no rate was found for the given street, the rate is 0

        // Convert Instants to LocalDateTimes in the specified time zone
        var startDateTime = LocalDateTime.ofInstant(session.getStartInstant(), ZoneId.of(parkingScheduleProperties.getZoneId()));
        var endDateTime = LocalDateTime.ofInstant(session.getEndInstant(), ZoneId.of(parkingScheduleProperties.getZoneId()));

        // Calculate total minutes in the chargeable period
        Long totalChargeableMinutes = calculateChargeableMinutes(startDateTime, endDateTime);

        // calculate the amount that needs to be paid for the session
        return totalChargeableMinutes * rate;
    }

    // Note: currently we are brute forcing the calculation of chargeable minutes by iterating over every minute in the session.
    // There may be a more efficient way to calculate this, but we're keeping it simple for now.
    private Long calculateChargeableMinutes(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        var totalChargeableMinutes = 0L;

        var current = startDateTime;
        var parkingPeriodStart = LocalTime.of(parkingScheduleProperties.getStartTime().getHour(), parkingScheduleProperties.getStartTime().getMinute()).minus(Duration.ofMillis(1));
        var parkingPeriodEnd = LocalTime.of(parkingScheduleProperties.getEndTime().getHour(), parkingScheduleProperties.getEndTime().getMinute());
        while (current.isBefore(endDateTime)) {

            if (current.toLocalTime().isAfter(parkingPeriodStart) // if the current minute is after the start of the parking period
                    && current.toLocalTime().isBefore(parkingPeriodEnd) // and the current minute is before the end of the parking period
                    && !current.getDayOfWeek().equals(DayOfWeek.SUNDAY)) { // and the current minute is not on a Sunday
                totalChargeableMinutes++; // increment the total chargeable minutes
            }

            // Move to the next minute
            current = current.plusMinutes(1);
        }

        return totalChargeableMinutes;
    }

    private Invoice mapStopParkingSessionResponse(ParkingInvoice invoice, ParkingSession session) {
        return Invoice.builder()
                .invoiceId(invoice.getId())
                .session(Session.builder()
                        .license(session.getLicense())
                        .street(session.getStreet())
                        .startInstant(session.getStartInstant())
                        .endInstant(session.getEndInstant())
                        .build())
                .invoiceInstant(invoice.getInvoiceInstant())
                .amount(invoice.getAmount())
                .paid(invoice.getPaid())
                .build();
    }
}
