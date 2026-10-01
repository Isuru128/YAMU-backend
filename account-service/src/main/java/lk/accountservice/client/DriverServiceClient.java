package lk.accountservice.client;

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
import java.util.Optional;

@Slf4j
@Component
public class DriverServiceClient {

    private final WebClient driverServiceWebClient;

    public DriverServiceClient(@Qualifier("driverServiceWebClient") WebClient driverServiceWebClient) {
        this.driverServiceWebClient = driverServiceWebClient;
    }

    public Optional<DriverProfileDto> getDriverByUserId(String userId) {
        try {
            DriverProfileDto driver = driverServiceWebClient.get()
                    .uri("/api/v1/drivers/user/{userId}", userId)
                    .retrieve()
                    .bodyToMono(DriverProfileDto.class)
                    .timeout(Duration.ofSeconds(4))
                    .block();
            return Optional.ofNullable(driver);
        } catch (WebClientResponseException.NotFound ex) {
            log.debug("No operational driver profile found for userId: {}", userId);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Failed to query Driver Service for userId {}: {}", userId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverProfileDto {
        private String id;
        private String userId;
        private String licenseNumber;
        private String availabilityStatus;
        private String serviceArea;
        private Double rating;
        private Integer totalTrips;
        private Boolean isApproved;
    }
}
