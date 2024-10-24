package abn.parking.core.controller;

import abn.parking.core.api.SessionsApi;
import abn.parking.core.dto.Invoice;
import abn.parking.core.dto.StartParkingSessionRequest;
import abn.parking.core.dto.StartParkingSessionResponse;
import abn.parking.core.service.SessionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SessionsController implements SessionsApi {

    private final SessionsService sessionsService;

    @Override
    public ResponseEntity<StartParkingSessionResponse> startParkingSession(String license, StartParkingSessionRequest startParkingSessionRequest) {
        var response = sessionsService.startParkingSession(license, startParkingSessionRequest);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Invoice> stopParkingSession(String license) {
        var response = sessionsService.stopParkingSession(license);

        return ResponseEntity.ok(response);
    }
}
