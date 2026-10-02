package lk.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lk.paymentservice.model.FareBreakdown;
import lk.paymentservice.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    private String passengerId;

    @NotBlank(message = "Driver ID is required")
    private String driverId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "1.0", message = "Payment amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private FareBreakdown fareBreakdown;

    // Optional field to simulate gateway failure for rubric negative scenario demonstration
    private Boolean simulateFailure;

    private String failureReason;
}
