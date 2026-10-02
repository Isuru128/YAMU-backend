package lk.paymentservice.dto;

import lk.paymentservice.model.FareBreakdown;
import lk.paymentservice.model.PaymentMethod;
import lk.paymentservice.model.Receipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {

    private String id;
    private String receiptNumber;
    private String paymentId;
    private String rideId;
    private String passengerId;
    private String driverId;
    private BigDecimal totalAmount;
    private String currency;
    private PaymentMethod paymentMethod;
    private FareBreakdown fareBreakdown;
    private Instant issuedAt;

    public static ReceiptResponse from(Receipt receipt) {
        if (receipt == null) {
            return null;
        }
        return ReceiptResponse.builder()
                .id(receipt.getId())
                .receiptNumber(receipt.getReceiptNumber())
                .paymentId(receipt.getPaymentId())
                .rideId(receipt.getRideId())
                .passengerId(receipt.getPassengerId())
                .driverId(receipt.getDriverId())
                .totalAmount(receipt.getTotalAmount())
                .currency(receipt.getCurrency())
                .paymentMethod(receipt.getPaymentMethod())
                .fareBreakdown(receipt.getFareBreakdown())
                .issuedAt(receipt.getIssuedAt())
                .build();
    }
}
