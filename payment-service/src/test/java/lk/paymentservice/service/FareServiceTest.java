package lk.paymentservice.service;

import lk.paymentservice.dto.CalculateFareRequest;
import lk.paymentservice.dto.FareEstimateRequest;
import lk.paymentservice.dto.FareEstimateResponse;
import lk.paymentservice.model.FareBreakdown;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FareServiceTest {

    private FareService fareService;

    @BeforeEach
    void setUp() {
        fareService = new FareServiceImpl();
    }

    @Test
    @DisplayName("Calculate estimate for CAR: base=200, km=100, min=5")
    void estimateFare_Car() {
        // Distance 10 km, Duration 20 min -> (200 + (10*100) + (20*5)) = 200 + 1000 + 100 = 1300.00
        FareEstimateRequest request = FareEstimateRequest.builder()
                .distanceKm(10.0)
                .durationMinutes(20.0)
                .vehicleType("CAR")
                .surgeMultiplier(BigDecimal.ONE)
                .build();

        FareEstimateResponse response = fareService.estimateFare(request);

        assertThat(response).isNotNull();
        assertThat(response.getEstimatedFare()).isEqualByComparingTo(BigDecimal.valueOf(1300.0));
        assertThat(response.getCurrency()).isEqualTo("LKR");
        assertThat(response.getFareBreakdown().getBaseFare()).isEqualByComparingTo(BigDecimal.valueOf(200.0));
    }

    @Test
    @DisplayName("Calculate estimate with surge pricing multiplier (1.5x)")
    void estimateFare_SurgeMultiplier() {
        // Distance 10 km, Duration 20 min in CAR = 1300 * 1.5 = 1950.00
        FareEstimateRequest request = FareEstimateRequest.builder()
                .distanceKm(10.0)
                .durationMinutes(20.0)
                .vehicleType("CAR")
                .surgeMultiplier(BigDecimal.valueOf(1.5))
                .build();

        FareEstimateResponse response = fareService.estimateFare(request);

        assertThat(response.getEstimatedFare()).isEqualByComparingTo(BigDecimal.valueOf(1950.0));
        assertThat(response.getFareBreakdown().getSurgeMultiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    @DisplayName("Calculate final fare for TUK: base=120, km=60, min=3")
    void calculateFinalFare_Tuk() {
        // 5 km, 10 min -> 120 + (5*60) + (10*3) = 120 + 300 + 30 = 450.00
        CalculateFareRequest request = CalculateFareRequest.builder()
                .rideId("ride-123")
                .distanceKm(5.0)
                .durationMinutes(10.0)
                .vehicleType("TUK")
                .build();

        FareBreakdown breakdown = fareService.calculateFinalFare(request);

        assertThat(breakdown.getTotalFare()).isEqualByComparingTo(BigDecimal.valueOf(450.0));
    }
}
