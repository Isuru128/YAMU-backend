package lk.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class CalculateFareRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotNull(message = "Actual distance in kilometers is required")
    @DecimalMin(value = "0.0", message = "Distance cannot be negative")
    private Double distanceKm;

    @NotNull(message = "Actual duration in minutes is required")
    @DecimalMin(value = "0.0", message = "Duration cannot be negative")
    private Double durationMinutes;

    private String vehicleType;

    private BigDecimal surgeMultiplier;
}
