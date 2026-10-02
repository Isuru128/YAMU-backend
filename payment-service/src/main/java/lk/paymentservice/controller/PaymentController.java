package lk.paymentservice.controller;

import jakarta.validation.Valid;
import lk.paymentservice.dto.PaymentResponse;
import lk.paymentservice.dto.ProcessPaymentRequest;
import lk.paymentservice.dto.ReceiptResponse;
import lk.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/payments", "/payments"})
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request,
                                                          Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : null;
        PaymentResponse response = paymentService.processPayment(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentResponse> getPaymentByRideId(@PathVariable String rideId) {
        PaymentResponse response = paymentService.getPaymentByRideId(rideId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<ReceiptResponse> getReceiptByPaymentId(@PathVariable String paymentId) {
        ReceiptResponse response = paymentService.getReceiptByPaymentId(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/receipts/ride/{rideId}")
    public ResponseEntity<ReceiptResponse> getReceiptByRideId(@PathVariable String rideId) {
        ReceiptResponse response = paymentService.getReceiptByRideId(rideId);
        return ResponseEntity.ok(response);
    }
}
