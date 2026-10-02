package lk.paymentservice.dto;

import lk.paymentservice.model.FareBreakdown;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateResponse {

    private BigDecimal estimatedFare;
    private String currency;
    private FareBreakdown fareBreakdown;
}
