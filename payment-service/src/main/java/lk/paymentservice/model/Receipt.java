package lk.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "receipts")
public class Receipt {

    @Id
    private String id;

    @Indexed(unique = true)
    private String receiptNumber;

    @Indexed(unique = true)
    private String paymentId;

    @Indexed
    private String rideId;

    private String passengerId;
    private String driverId;

    private BigDecimal totalAmount;
    private String currency;

    private PaymentMethod paymentMethod;
    private FareBreakdown fareBreakdown;

    @CreatedDate
    private Instant issuedAt;
}
