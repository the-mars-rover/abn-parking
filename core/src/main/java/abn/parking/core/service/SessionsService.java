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
import org.springframework.transaction.support.TransactionTemplate;

import java.time.*;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SessionsService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingRateRepository parkingRateRepository;
    private final ParkingInvoiceRepository parkingInvoiceRepository;
    private final ParkingScheduleProperties parkingScheduleProperties;
    private final TransactionTemplate transactionTemplate;

    public GetOpenSessionResponse getOpenSession(String license) {
        // get session by license (the NoSuchElementException will lead to a 404 response if no session was found)
        var session = parkingSessionRepository.findByLicenseAndEndInstantIsNull(license).orElseThrow();

        // return response
        return GetOpenSessionResponse.builder()
                .sessionId(session.getId())
                .license(session.getLicense())
                .street(session.getStreet())
                .startInstant(session.getStartInstant())
                .build();
    }

    public StartParkingSessionResponse startParkingSession(StartParkingSessionRequest startParkingSessionRequest) {
        // create the session entity
        var session = new ParkingSession();
        session.setLicense(startParkingSessionRequest.getLicense());
        session.setStreet(startParkingSessionRequest.getStreet());
        session.setStartInstant(Instant.now());

        // save the session
        var savedSession = parkingSessionRepository.save(session);

        // map the saved session to the response and return it
        return StartParkingSessionResponse.builder()
                .sessionId(savedSession.getId())
                .license(savedSession.getLicense())
                .street(savedSession.getStreet())
                .startInstant(savedSession.getStartInstant())
                .build();
    }

    @Transactional
    public Invoice stopParkingSession(Long sessionId) {
        // get the session by id (the NoSuchElementException will lead to a 404 response if no session was found)
        var session = parkingSessionRepository.findById(sessionId).orElseThrow();

        // set the end instant for the session and save it
        session.setEndInstant(Instant.now());
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
        invoice.setInvoiceInstant(Instant.now());
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

    private Long calculateChargeableMinutes(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        var totalChargeableMinutes = 0L;

        var current = startDateTime;
        while (current.isBefore(endDateTime)) {
            // 21-10-2024 08:00
            var periodStart = current.toLocalDate().atTime(LocalTime.of(parkingScheduleProperties.getStartTime().getHour(), parkingScheduleProperties.getStartTime().getMinute()));
            // 22-10-2024 21:00
            var periodEnd = current.toLocalDate().atTime(LocalTime.of(parkingScheduleProperties.getEndTime().getHour(), parkingScheduleProperties.getEndTime().getMinute()));

            // Adjust period boundaries if session starts or ends in the middle of the period
            var effectiveStart = current.isAfter(periodStart) ? current : periodStart; // 21-10-2024 08:00
            var effectiveEnd = endDateTime.isBefore(periodEnd) ? endDateTime : periodEnd; // 21-10-2024 21:00

            // Calculate the overlap between the effective session time and the chargeable period
            if (!effectiveStart.isAfter(effectiveEnd)) {
                long minutes = ChronoUnit.MINUTES.between(effectiveStart, effectiveEnd);
                totalChargeableMinutes += minutes;
            }

            // Move to the next day
            current = current.toLocalDate().plusDays(1).atStartOfDay();
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
