package lk.paymentservice.service;

import lk.paymentservice.dto.PaymentResponse;
import lk.paymentservice.dto.ProcessPaymentRequest;
import lk.paymentservice.dto.ReceiptResponse;
import lk.paymentservice.exception.PaymentAlreadyProcessedException;
import lk.paymentservice.exception.PaymentFailedException;
import lk.paymentservice.exception.PaymentNotFoundException;
import lk.paymentservice.exception.ReceiptNotFoundException;
import lk.paymentservice.model.Payment;
import lk.paymentservice.model.PaymentStatus;
import lk.paymentservice.model.Receipt;
import lk.paymentservice.repository.PaymentRepository;
import lk.paymentservice.repository.ReceiptRepository;
import lk.paymentservice.client.RideServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final RideServiceClient rideServiceClient;

    @Override
    public PaymentResponse processPayment(ProcessPaymentRequest request, String authenticatedUserId) {
        String effectivePassengerId = request.getPassengerId() != null && !request.getPassengerId().isBlank()
                ? request.getPassengerId()
                : authenticatedUserId;

        if (paymentRepository.existsByRideIdAndStatus(request.getRideId(), PaymentStatus.COMPLETED)) {
            throw new PaymentAlreadyProcessedException("A completed payment already exists for ride ID: " + request.getRideId());
        }

        // Simulate failure trigger for negative workflow demonstration (Workflow 7)
        if (Boolean.TRUE.equals(request.getSimulateFailure())) {
            String reason = request.getFailureReason() != null && !request.getFailureReason().isBlank()
                    ? request.getFailureReason()
                    : "INSUFFICIENT_FUNDS";

            Payment failedPayment = Payment.builder()
                    .rideId(request.getRideId())
                    .passengerId(effectivePassengerId)
                    .driverId(request.getDriverId())
                    .amount(request.getAmount())
                    .currency("LKR")
                    .paymentMethod(request.getPaymentMethod())
                    .status(PaymentStatus.FAILED)
                    .failureReason(reason)
                    .fareBreakdown(request.getFareBreakdown())
                    .build();

            Payment savedFailedPayment = paymentRepository.save(failedPayment);
            log.warn("Payment failed for rideId: {} with reason: {}", request.getRideId(), reason);
            throw new PaymentFailedException(String.format("Payment failed (ID: %s): %s", savedFailedPayment.getId(), reason));
        }

        Instant now = Instant.now();
        String txnRef = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Payment payment = Payment.builder()
                .rideId(request.getRideId())
                .passengerId(effectivePassengerId)
                .driverId(request.getDriverId())
                .amount(request.getAmount())
                .currency("LKR")
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.COMPLETED)
                .transactionReference(txnRef)
                .fareBreakdown(request.getFareBreakdown())
                .paidAt(now)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Recorded payment ID: {} for rideId: {}, amount: {}", savedPayment.getId(), request.getRideId(), request.getAmount());

        // Automatically issue digital receipt
        String receiptNumber = "REC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        Receipt receipt = Receipt.builder()
                .receiptNumber(receiptNumber)
                .paymentId(savedPayment.getId())
                .rideId(request.getRideId())
                .passengerId(effectivePassengerId)
                .driverId(request.getDriverId())
                .totalAmount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .paymentMethod(savedPayment.getPaymentMethod())
                .fareBreakdown(savedPayment.getFareBreakdown())
                .issuedAt(now)
                .build();

        receiptRepository.save(receipt);
        log.info("Issued digital receipt: {} for payment: {}", receiptNumber, savedPayment.getId());

        // Inter-service notification: bind payment ID to ride in Ride Management Service
        rideServiceClient.updateRidePayment(request.getRideId(), savedPayment.getId(), savedPayment.getAmount().doubleValue());

        return PaymentResponse.from(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse getPaymentByRideId(String rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ride ID: " + rideId));
        return PaymentResponse.from(payment);
    }

    @Override
    public ReceiptResponse getReceiptByPaymentId(String paymentId) {
        Receipt receipt = receiptRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ReceiptNotFoundException("Receipt not found for payment ID: " + paymentId));
        return ReceiptResponse.from(receipt);
    }

    @Override
    public ReceiptResponse getReceiptByRideId(String rideId) {
        Receipt receipt = receiptRepository.findByRideId(rideId)
                .orElseThrow(() -> new ReceiptNotFoundException("Receipt not found for ride ID: " + rideId));
        return ReceiptResponse.from(receipt);
    }
}
