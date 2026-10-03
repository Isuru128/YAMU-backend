package lk.ridemanagementservice.service;

import lk.ridemanagementservice.client.DriverServiceClient;
import lk.ridemanagementservice.client.PaymentServiceClient;
import lk.ridemanagementservice.dto.AssignDriverRequest;
import lk.ridemanagementservice.dto.CancelRideRequest;
import lk.ridemanagementservice.dto.CompleteRideRequest;
import lk.ridemanagementservice.dto.CreateRideRequest;
import lk.ridemanagementservice.dto.RideResponse;
import lk.ridemanagementservice.exception.InvalidRideStateTransitionException;
import lk.ridemanagementservice.exception.UnauthorizedRideActionException;
import lk.ridemanagementservice.model.Ride;
import lk.ridemanagementservice.model.RideLocation;
import lk.ridemanagementservice.model.RideStatus;
import lk.ridemanagementservice.model.VehicleType;
import lk.ridemanagementservice.repository.RideRepository;
import lk.ridemanagementservice.repository.RideStatusHistoryRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RideStatusHistoryRepository statusHistoryRepository;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private DriverServiceClient driverServiceClient;

    @InjectMocks
    private RideServiceImpl rideService;

    private Ride sampleRide;

    @BeforeEach
    void setUp() {
        sampleRide = Ride.builder()
                .id("ride-500")
                .passengerId("user-passenger-1")
                .driverId("drv-1")
                .pickupLocation(RideLocation.builder().address("Colombo 03").latitude(6.90).longitude(79.85).build())
                .destinationLocation(RideLocation.builder().address("Colombo Fort").latitude(6.93).longitude(79.84).build())
                .requestedVehicleType(VehicleType.CAR)
                .status(RideStatus.REQUESTED)
                .estimatedDistanceKm(5.0)
                .estimatedDurationMinutes(15.0)
                .estimatedFare(500.0)
                .build();
    }

    @Test
    @DisplayName("Successfully create ride request and call PaymentServiceClient for estimation if missing")
    void createRide_Success() {
        CreateRideRequest request = CreateRideRequest.builder()
                .passengerId("user-passenger-1")
                .pickupLocation(sampleRide.getPickupLocation())
                .destinationLocation(sampleRide.getDestinationLocation())
                .requestedVehicleType(VehicleType.CAR)
                .estimatedDistanceKm(5.0)
                .estimatedDurationMinutes(15.0)
                .build();

        PaymentServiceClient.FareEstimateResponseDto estimateDto = PaymentServiceClient.FareEstimateResponseDto.builder()
                .estimatedFare(BigDecimal.valueOf(500.0))
                .currency("LKR")
                .build();

        when(paymentServiceClient.estimateFare(5.0, 15.0, "CAR")).thenReturn(Optional.of(estimateDto));
        when(rideRepository.save(any(Ride.class))).thenReturn(sampleRide);

        RideResponse response = rideService.createRide(request, "user-passenger-1");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(RideStatus.REQUESTED);
        verify(paymentServiceClient, times(1)).estimateFare(5.0, 15.0, "CAR");
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    @DisplayName("Assign driver to ride in REQUESTED state")
    void assignDriver_Success() {
        AssignDriverRequest request = AssignDriverRequest.builder()
                .driverId("drv-1")
                .build();

        when(rideRepository.findById("ride-500")).thenReturn(Optional.of(sampleRide));
        when(driverServiceClient.getDriverById("drv-1")).thenReturn(Optional.of(DriverServiceClient.DriverDto.builder().id("drv-1").availabilityStatus("AVAILABLE").build()));
        when(rideRepository.save(any(Ride.class))).thenReturn(sampleRide);

        RideResponse response = rideService.assignDriver("ride-500", request, "user-passenger-1", false);

        assertThat(response).isNotNull();
        assertThat(sampleRide.getStatus()).isEqualTo(RideStatus.ASSIGNED);
        assertThat(sampleRide.getDriverId()).isEqualTo("drv-1");
    }

    @Test
    @DisplayName("Fail assigning driver if ride not in REQUESTED state (Negative Scenario)")
    void assignDriver_InvalidState() {
        sampleRide.setStatus(RideStatus.IN_PROGRESS);
        AssignDriverRequest request = AssignDriverRequest.builder().driverId("drv-1").build();

        when(rideRepository.findById("ride-500")).thenReturn(Optional.of(sampleRide));

        assertThrows(InvalidRideStateTransitionException.class, () ->
                rideService.assignDriver("ride-500", request, "admin", true)
        );
    }

    @Test
    @DisplayName("Driver accepts assigned ride")
    void acceptRide_Success() {
        sampleRide.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-500")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(sampleRide);

        RideResponse response = rideService.acceptRide("ride-500", "drv-1", false);

        assertThat(sampleRide.getStatus()).isEqualTo(RideStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Fail accept ride if unauthorized driver attempts to accept (Negative Scenario)")
    void acceptRide_UnauthorizedDriver() {
        sampleRide.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("ride-500")).thenReturn(Optional.of(sampleRide));

        assertThrows(UnauthorizedRideActionException.class, () ->
                rideService.acceptRide("ride-500", "wrong-driver-id", false)
        );
    }

    @Test
    @DisplayName("Start ride changes status to IN_PROGRESS and updates driver availability to ON_TRIP")
    void startRide_Success() {
        sampleRide.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.findById("ride-500")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(sampleRide);

        RideResponse response = rideService.startRide("ride-500", "drv-1", false);

        assertThat(sampleRide.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);
        verify(driverServiceClient, times(1)).updateDriverAvailability("drv-1", "ON_TRIP");
    }

    @Test
    @DisplayName("Complete ride sets COMPLETED, calculates final fare, and releases driver to AVAILABLE")
    void completeRide_Success() {
        sampleRide.setStatus(RideStatus.IN_PROGRESS);
        CompleteRideRequest request = CompleteRideRequest.builder()
                .actualDistanceKm(5.2)
                .actualDurationMinutes(16.0)
                .build();

        PaymentServiceClient.FareBreakdownDto fareDto = PaymentServiceClient.FareBreakdownDto.builder()
                .totalFare(BigDecimal.valueOf(550.0))
                .build();

        when(rideRepository.findById("ride-500")).thenReturn(Optional.of(sampleRide));
        when(paymentServiceClient.calculateFinalFare(eq("ride-500"), eq(5.2), eq(16.0), eq("CAR")))
                .thenReturn(Optional.of(fareDto));
        when(rideRepository.save(any(Ride.class))).thenReturn(sampleRide);

        RideResponse response = rideService.completeRide("ride-500", request, "drv-1", false);

        assertThat(sampleRide.getStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(sampleRide.getFinalFare()).isEqualTo(550.0);
        verify(driverServiceClient, times(1)).updateDriverAvailability("drv-1", "AVAILABLE");
    }

    @Test
    @DisplayName("Cancel ride releases driver to AVAILABLE")
    void cancelRide_Success() {
        sampleRide.setStatus(RideStatus.ASSIGNED);
        CancelRideRequest request = CancelRideRequest.builder().cancellationReason("Changed mind").build();

        when(rideRepository.findById("ride-500")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(sampleRide);

        RideResponse response = rideService.cancelRide("ride-500", request, "user-passenger-1", false);

        assertThat(sampleRide.getStatus()).isEqualTo(RideStatus.CANCELLED);
        verify(driverServiceClient, times(1)).updateDriverAvailability("drv-1", "AVAILABLE");
    }

    @Test
    @DisplayName("Fail cancelling an already IN_PROGRESS ride (Negative Scenario)")
    void cancelRide_InProgressThrowsException() {
        sampleRide.setStatus(RideStatus.IN_PROGRESS);
        CancelRideRequest request = CancelRideRequest.builder().cancellationReason("Too slow").build();

        when(rideRepository.findById("ride-500")).thenReturn(Optional.of(sampleRide));

        assertThrows(InvalidRideStateTransitionException.class, () ->
                rideService.cancelRide("ride-500", request, "user-passenger-1", false)
        );
    }
}
