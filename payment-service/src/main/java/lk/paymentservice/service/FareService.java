package lk.paymentservice.service;

import lk.paymentservice.dto.CalculateFareRequest;
import lk.paymentservice.dto.FareEstimateRequest;
import lk.paymentservice.dto.FareEstimateResponse;
import lk.paymentservice.model.FareBreakdown;

public interface FareService {

    FareEstimateResponse estimateFare(FareEstimateRequest request);

    FareBreakdown calculateFinalFare(CalculateFareRequest request);
}
