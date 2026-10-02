package lk.paymentservice.service;

import lk.paymentservice.client.RideServiceClient;
import lk.paymentservice.dto.PaymentResponse;
import lk.paymentservice.dto.ProcessPaymentRequest;
import lk.paymentservice.dto.ReceiptResponse;
import lk.paymentservice.exception.PaymentAlreadyProcessedException;
import lk.paymentservice.exception.PaymentFailedException;
import lk.paymentservice.model.Payment;
import lk.paymentservice.model.PaymentMethod;
import lk.paymentservice.model.PaymentStatus;
import lk.paymentservice.model.Receipt;
import lk.paymentservice.repository.PaymentRepository;
import lk.paymentservice.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private RideServiceClient rideServiceClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        samplePayment = Payment.builder()
                .id("pay-999")
                .rideId("ride-500")
                .passengerId("user-passenger-1")
                .driverId("drv-1")
                .amount(BigDecimal.valueOf(750.0))
                .currency("LKR")
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionReference("TXN-12345")
                .build();
    }

    @Test
    @DisplayName("Successfully process payment, generate receipt and notify Ride Service")
    void processPayment_Success() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .rideId("ride-500")
                .passengerId("user-passenger-1")
                .driverId("drv-1")
                .amount(BigDecimal.valueOf(750.0))
                .paymentMethod(PaymentMethod.CARD)
                .build();

        when(paymentRepository.existsByRideIdAndStatus("ride-500", PaymentStatus.COMPLETED)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(samplePayment);
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(i -> i.getArgument(0));

        PaymentResponse response = paymentService.processPayment(request, "user-passenger-1");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(receiptRepository, times(1)).save(any(Receipt.class));
        verify(rideServiceClient, times(1)).updateRidePayment(eq("ride-500"), eq("pay-999"), eq(750.0));
    }

    @Test
    @DisplayName("Fail payment when simulateFailure is triggered (Negative Scenario)")
    void processPayment_SimulateFailure() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .rideId("ride-500")
                .passengerId("user-passenger-1")
                .driverId("drv-1")
                .amount(BigDecimal.valueOf(750.0))
                .paymentMethod(PaymentMethod.CARD)
                .simulateFailure(true)
                .failureReason("INSUFFICIENT_FUNDS")
                .build();

        when(paymentRepository.existsByRideIdAndStatus("ride-500", PaymentStatus.COMPLETED)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId("failed-pay-1");
            return p;
        });

        assertThrows(PaymentFailedException.class, () ->
                paymentService.processPayment(request, "user-passenger-1")
        );
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test
    @DisplayName("Fail duplicate payment if already completed for ride (Negative Scenario)")
    void processPayment_AlreadyProcessed() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .rideId("ride-500")
                .driverId("drv-1")
                .amount(BigDecimal.valueOf(750.0))
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(paymentRepository.existsByRideIdAndStatus("ride-500", PaymentStatus.COMPLETED)).thenReturn(true);

        assertThrows(PaymentAlreadyProcessedException.class, () ->
                paymentService.processPayment(request, "user-passenger-1")
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Retrieve receipt by ride ID")
    void getReceiptByRideId_Success() {
        Receipt receipt = Receipt.builder()
                .id("rcpt-1")
                .receiptNumber("REC-123")
                .paymentId("pay-999")
                .rideId("ride-500")
                .totalAmount(BigDecimal.valueOf(750.0))
                .currency("LKR")
                .build();

        when(receiptRepository.findByRideId("ride-500")).thenReturn(Optional.of(receipt));

        ReceiptResponse response = paymentService.getReceiptByRideId("ride-500");

        assertThat(response.getReceiptNumber()).isEqualTo("REC-123");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(750.0));
    }
}
