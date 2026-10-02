package lk.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateRequest {

    @NotNull(message = "Distance in kilometers is required")
    @DecimalMin(value = "0.1", message = "Distance must be greater than 0")
    private Double distanceKm;

    @NotNull(message = "Duration in minutes is required")
    @DecimalMin(value = "1.0", message = "Duration must be at least 1 minute")
    private Double durationMinutes;

    private String vehicleType;

    private BigDecimal surgeMultiplier;
}
