package lk.paymentservice.repository;

import lk.paymentservice.model.Payment;
import lk.paymentservice.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByRideId(String rideId);

    Optional<Payment> findByTransactionReference(String transactionReference);

    List<Payment> findByPassengerIdOrderByCreatedAtDesc(String passengerId);

    List<Payment> findByDriverIdOrderByCreatedAtDesc(String driverId);

    boolean existsByRideIdAndStatus(String rideId, PaymentStatus status);
}
