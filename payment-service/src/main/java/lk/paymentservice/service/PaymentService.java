package lk.paymentservice.service;

import lk.paymentservice.dto.PaymentResponse;
import lk.paymentservice.dto.ProcessPaymentRequest;
import lk.paymentservice.dto.ReceiptResponse;

public interface PaymentService {

    PaymentResponse processPayment(ProcessPaymentRequest request, String authenticatedUserId);

    PaymentResponse getPaymentById(String paymentId);

    PaymentResponse getPaymentByRideId(String rideId);

    ReceiptResponse getReceiptByPaymentId(String paymentId);

    ReceiptResponse getReceiptByRideId(String rideId);
}
