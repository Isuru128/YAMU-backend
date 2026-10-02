package lk.paymentservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class RideServiceClient {

    private final WebClient rideServiceWebClient;

    public RideServiceClient(@Qualifier("rideServiceWebClient") WebClient rideServiceWebClient) {
        this.rideServiceWebClient = rideServiceWebClient;
    }

    public Optional<RideDto> getRideById(String rideId) {
        try {
            RideDto ride = rideServiceWebClient.get()
                    .uri("/api/v1/rides/{rideId}", rideId)
                    .retrieve()
                    .bodyToMono(RideDto.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();
            return Optional.ofNullable(ride);
        } catch (WebClientResponseException.NotFound ex) {
            log.warn("Ride not found in Ride Service: {}", rideId);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Failed to query Ride Service for rideId {}: {}", rideId, ex.getMessage());
            return Optional.empty();
        }
    }

    public boolean updateRidePayment(String rideId, String paymentId, Double finalFare) {
        try {
            rideServiceWebClient.patch()
                    .uri("/api/v1/rides/{rideId}/complete", rideId)
                    .bodyValue(Map.of(
                            "paymentId", paymentId,
                            "finalFare", finalFare != null ? finalFare : 0.0
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(4))
                    .block();
            log.info("Successfully updated ride {} with payment {}", rideId, paymentId);
            return true;
        } catch (Exception ex) {
            log.warn("Failed to notify Ride Service for payment {}: {}", paymentId, ex.getMessage());
            return false;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RideDto {
        private String id;
        private String passengerId;
        private String driverId;
        private String status;
        private Double finalFare;
        private String paymentId;
    }
}
