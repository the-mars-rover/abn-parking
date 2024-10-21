package abn.parking.core.service;

import abn.parking.core.dto.GetInvoicesResponse;
import abn.parking.core.dto.Invoice;
import abn.parking.core.dto.Observation;
import abn.parking.core.dto.Session;
import abn.parking.core.entity.ParkingInvoice;
import abn.parking.core.repository.ParkingInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoicesService {

    private final ParkingInvoiceRepository parkingInvoiceRepository;

    public GetInvoicesResponse getInvoices(String license) {
        // Get all invoices for the given license
        var invoices = parkingInvoiceRepository.findAllBySessionLicenseOrObservationLicense(license, license);

        // Map the invoices to the GetInvoicesResponse and return it
        return mapResponse(invoices);
    }

    public void payInvoice(Long invoiceId) {
        // Get the invoice by id (the NoSuchElementException will lead to a 404 response if no session was found)
        var invoice = parkingInvoiceRepository.findById(invoiceId).orElseThrow();

        // Set the invoice as paid
        invoice.setPaid(true);

        // Save the invoice
        parkingInvoiceRepository.save(invoice);
    }

    private static GetInvoicesResponse mapResponse(List<ParkingInvoice> invoices) {
        return GetInvoicesResponse.builder()
                .invoices(invoices.stream().map(invoice -> Invoice.builder()
                        .invoiceId(invoice.getId())
                        .invoiceInstant(invoice.getInvoiceInstant())
                        .session(mapSession(invoice))
                        .observation(mapObservation(invoice))
                        .amount(invoice.getAmount())
                        .paid(invoice.getPaid())
                        .build()).toList()).build();
    }

    private static Observation mapObservation(ParkingInvoice invoice) {
        if (invoice.getObservation() == null) {
            return null;
        }

        return Observation.builder()
                .license(invoice.getObservation().getLicense())
                .street(invoice.getObservation().getStreet())
                .observationInstant(invoice.getObservation().getObservationInstant())
                .build();
    }

    private static Session mapSession(ParkingInvoice invoice) {
        if (invoice.getSession() == null) {
            return null;
        }

        return Session.builder()
                .license(invoice.getSession().getLicense())
                .street(invoice.getSession().getStreet())
                .startInstant(invoice.getSession().getStartInstant())
                .endInstant(invoice.getSession().getEndInstant())
                .build();
    }

}
