package lk.paymentservice.service;

import lk.paymentservice.dto.CalculateFareRequest;
import lk.paymentservice.dto.FareEstimateRequest;
import lk.paymentservice.dto.FareEstimateResponse;
import lk.paymentservice.model.FareBreakdown;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class FareServiceImpl implements FareService {

    private static final String DEFAULT_CURRENCY = "LKR";

    @Override
    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        FareBreakdown breakdown = computeFareBreakdown(
                request.getDistanceKm(),
                request.getDurationMinutes(),
                request.getVehicleType(),
                request.getSurgeMultiplier()
        );

        return FareEstimateResponse.builder()
                .estimatedFare(breakdown.getTotalFare())
                .currency(DEFAULT_CURRENCY)
                .fareBreakdown(breakdown)
                .build();
    }

    @Override
    public FareBreakdown calculateFinalFare(CalculateFareRequest request) {
        log.info("Calculating final fare for ride: {}", request.getRideId());
        return computeFareBreakdown(
                request.getDistanceKm(),
                request.getDurationMinutes(),
                request.getVehicleType(),
                request.getSurgeMultiplier()
        );
    }

    private FareBreakdown computeFareBreakdown(Double distanceKm, Double durationMinutes, String vehicleType, BigDecimal surgeMultiplier) {
        BigDecimal baseFare;
        BigDecimal ratePerKm;
        BigDecimal ratePerMin;

        String type = vehicleType != null ? vehicleType.trim().toUpperCase() : "CAR";

        switch (type) {
            case "VAN":
                baseFare = BigDecimal.valueOf(350.0);
                ratePerKm = BigDecimal.valueOf(140.0);
                ratePerMin = BigDecimal.valueOf(8.0);
                break;
            case "TUK":
                baseFare = BigDecimal.valueOf(120.0);
                ratePerKm = BigDecimal.valueOf(60.0);
                ratePerMin = BigDecimal.valueOf(3.0);
                break;
            case "BIKE":
                baseFare = BigDecimal.valueOf(80.0);
                ratePerKm = BigDecimal.valueOf(40.0);
                ratePerMin = BigDecimal.valueOf(2.0);
                break;
            case "CAR":
            default:
                baseFare = BigDecimal.valueOf(200.0);
                ratePerKm = BigDecimal.valueOf(100.0);
                ratePerMin = BigDecimal.valueOf(5.0);
                break;
        }

        BigDecimal distance = BigDecimal.valueOf(distanceKm != null ? distanceKm : 0.0);
        BigDecimal duration = BigDecimal.valueOf(durationMinutes != null ? durationMinutes : 0.0);

        BigDecimal distanceFare = distance.multiply(ratePerKm).setScale(2, RoundingMode.HALF_UP);
        BigDecimal timeFare = duration.multiply(ratePerMin).setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotal = baseFare.add(distanceFare).add(timeFare);

        BigDecimal surge = (surgeMultiplier != null && surgeMultiplier.compareTo(BigDecimal.ZERO) > 0)
                ? surgeMultiplier
                : BigDecimal.ONE;

        BigDecimal totalFare = subtotal.multiply(surge).setScale(2, RoundingMode.HALF_UP);

        return FareBreakdown.builder()
                .baseFare(baseFare)
                .distanceFare(distanceFare)
                .timeFare(timeFare)
                .surgeMultiplier(surge)
                .totalFare(totalFare)
                .currency(DEFAULT_CURRENCY)
                .build();
    }
}
