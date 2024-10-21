package abn.parking.core.controller;

import abn.parking.core.api.SessionsApi;
import abn.parking.core.dto.GetOpenSessionResponse;
import abn.parking.core.dto.Invoice;
import abn.parking.core.dto.StartParkingSessionRequest;
import abn.parking.core.dto.StartParkingSessionResponse;
import abn.parking.core.service.SessionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class SessionsController implements SessionsApi {

    private final SessionsService sessionsService;

    @Override
    public ResponseEntity<GetOpenSessionResponse> getOpenSession(String license) {
        var response = sessionsService.getOpenSession(license);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<StartParkingSessionResponse> startParkingSession(StartParkingSessionRequest startParkingSessionRequest) {
        var response = sessionsService.startParkingSession(startParkingSessionRequest);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Invoice> stopParkingSession(Long sessionId) {
        var response = sessionsService.stopParkingSession(sessionId);

        return ResponseEntity.ok(response);
    }
}
