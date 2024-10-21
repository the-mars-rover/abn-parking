package abn.parking.core.controller;

import abn.parking.core.api.InvoicesApi;
import abn.parking.core.dto.GetInvoicesResponse;
import abn.parking.core.service.InvoicesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class InvoicesController implements InvoicesApi {

    private final InvoicesService invoicesService;

    @Override
    public ResponseEntity<GetInvoicesResponse> getInvoices(String license) {
        var response = invoicesService.getInvoices(license);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> payInvoice(Long invoiceId) {
        invoicesService.payInvoice(invoiceId);

        return ResponseEntity.ok().build();
    }
}
