package lk.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    // Stable global ride identifier from Ride Management Service
    @Indexed
    private String rideId;

    // Stable global passenger identifier from Account Service
    @Indexed
    private String passengerId;

    // Stable global driver identifier from Account / Driver Service
    @Indexed
    private String driverId;

    private BigDecimal amount;

    @Builder.Default
    private String currency = "LKR";

    private PaymentMethod paymentMethod;

    @Indexed
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // Simulated transaction reference generated on payment completion
    private String transactionReference;

    private FareBreakdown fareBreakdown;

    // Failure reason for negative test scenarios (e.g. INSUFFICIENT_FUNDS)
    private String failureReason;

    private Instant paidAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
