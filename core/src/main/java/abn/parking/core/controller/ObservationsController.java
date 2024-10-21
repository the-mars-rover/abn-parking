package abn.parking.core.controller;

import abn.parking.core.api.ObservationsApi;
import abn.parking.core.dto.AddObservationsRequest;
import abn.parking.core.service.ObservationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ObservationsController implements ObservationsApi {

    private final ObservationsService observationsService;

    @Override
    public ResponseEntity<Void> addObservations(AddObservationsRequest addObservationsRequest) {
        observationsService.addObservations(addObservationsRequest);

        return ResponseEntity.ok().build();
    }
}
