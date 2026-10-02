package lk.paymentservice.controller;

import jakarta.validation.Valid;
import lk.paymentservice.dto.CalculateFareRequest;
import lk.paymentservice.dto.FareEstimateRequest;
import lk.paymentservice.dto.FareEstimateResponse;
import lk.paymentservice.model.FareBreakdown;
import lk.paymentservice.service.FareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/fares", "/fares"})
@RequiredArgsConstructor
public class FareController {

    private final FareService fareService;

    @PostMapping("/estimate")
    public ResponseEntity<FareEstimateResponse> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        FareEstimateResponse response = fareService.estimateFare(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/calculate")
    public ResponseEntity<FareBreakdown> calculateFinalFare(@Valid @RequestBody CalculateFareRequest request) {
        FareBreakdown response = fareService.calculateFinalFare(request);
        return ResponseEntity.ok(response);
    }
}
