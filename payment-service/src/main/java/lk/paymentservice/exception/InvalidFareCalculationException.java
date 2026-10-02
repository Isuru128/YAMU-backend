package lk.paymentservice.exception;

public class InvalidFareCalculationException extends RuntimeException {
    public InvalidFareCalculationException(String message) {
        super(message);
    }
}
