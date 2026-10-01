package lk.driverandvehicleservice.service;

import lk.driverandvehicleservice.client.AccountServiceClient;
import lk.driverandvehicleservice.dto.DriverResponse;
import lk.driverandvehicleservice.dto.RegisterDriverRequest;
import lk.driverandvehicleservice.dto.UpdateAvailabilityRequest;
import lk.driverandvehicleservice.dto.UpdateLocationRequest;
import lk.driverandvehicleservice.dto.UpdateVehicleRequest;
import lk.driverandvehicleservice.exception.DriverAlreadyExistsException;
import lk.driverandvehicleservice.exception.DriverNotFoundException;
import lk.driverandvehicleservice.exception.LicenseAlreadyRegisteredException;
import lk.driverandvehicleservice.model.AvailabilityStatus;
import lk.driverandvehicleservice.model.Driver;
import lk.driverandvehicleservice.model.Location;
import lk.driverandvehicleservice.model.Vehicle;
import lk.driverandvehicleservice.model.VehicleType;
import lk.driverandvehicleservice.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private DriverServiceImpl driverService;

    private Driver sampleDriver;

    @BeforeEach
    void setUp() {
        sampleDriver = Driver.builder()
                .id("drv-1")
                .userId("acc-driver-1")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .vehicle(Vehicle.builder()
                        .licensePlate("WP-CAR-1122")
                        .make("Toyota")
                        .model("Prius")
                        .vehicleType(VehicleType.CAR)
                        .capacity(4)
                        .build())
                .currentLocation(Location.builder().latitude(6.9271).longitude(79.8612).address("Colombo 03").build())
                .rating(5.0)
                .totalTrips(10)
                .isApproved(true)
                .build();
    }

    @Test
    @DisplayName("Successfully register driver operational profile")
    void registerDriver_Success() {
        RegisterDriverRequest request = RegisterDriverRequest.builder()
                .userId("acc-driver-1")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .vehicle(sampleDriver.getVehicle())
                .build();

        AccountServiceClient.AccountDto mockAccount = new AccountServiceClient.AccountDto();
        mockAccount.setId("acc-driver-1");
        mockAccount.setRole("DRIVER");
        when(accountServiceClient.getAccountById("acc-driver-1")).thenReturn(Optional.of(mockAccount));

        when(driverRepository.existsByUserId("acc-driver-1")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("B1234567")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);

        DriverResponse response = driverService.registerDriver(request, "acc-driver-1");

        assertThat(response).isNotNull();
        assertThat(response.getLicenseNumber()).isEqualTo("B1234567");
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    @DisplayName("Fail driver registration if user already has driver profile (Negative Scenario)")
    void registerDriver_AlreadyExists() {
        RegisterDriverRequest request = RegisterDriverRequest.builder()
                .userId("acc-driver-1")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .build();

        when(driverRepository.existsByUserId("acc-driver-1")).thenReturn(true);

        assertThrows(DriverAlreadyExistsException.class, () -> driverService.registerDriver(request, "acc-driver-1"));
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Fail driver registration if license is duplicate (Negative Scenario)")
    void registerDriver_LicenseDuplicate() {
        RegisterDriverRequest request = RegisterDriverRequest.builder()
                .userId("acc-driver-2")
                .licenseNumber("B1234567")
                .serviceArea("Colombo")
                .build();

        when(driverRepository.existsByUserId("acc-driver-2")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("B1234567")).thenReturn(true);

        assertThrows(LicenseAlreadyRegisteredException.class, () -> driverService.registerDriver(request, "acc-driver-2"));
    }

    @Test
    @DisplayName("Update driver availability status")
    void updateAvailability_Success() {
        UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                .availabilityStatus(AvailabilityStatus.ON_TRIP)
                .build();

        when(driverRepository.findById("drv-1")).thenReturn(Optional.of(sampleDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);

        DriverResponse response = driverService.updateAvailability("drv-1", request, "acc-driver-1", false);

        assertThat(response.getAvailabilityStatus()).isEqualTo(AvailabilityStatus.ON_TRIP);
    }

    @Test
    @DisplayName("Update driver simulated GPS location")
    void updateLocation_Success() {
        UpdateLocationRequest request = UpdateLocationRequest.builder()
                .latitude(6.9319)
                .longitude(79.8478)
                .address("Colombo Fort")
                .build();

        when(driverRepository.findById("drv-1")).thenReturn(Optional.of(sampleDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);

        DriverResponse response = driverService.updateLocation("drv-1", request, "acc-driver-1", false);

        assertThat(response.getCurrentLocation().getLatitude()).isEqualTo(6.9319);
        assertThat(response.getCurrentLocation().getAddress()).isEqualTo("Colombo Fort");
    }

    @Test
    @DisplayName("Query available drivers by service area and vehicle type")
    void getAvailableDrivers_Filter() {
        when(driverRepository.findByAvailabilityStatusAndServiceAreaIgnoreCaseAndVehicle_VehicleType(AvailabilityStatus.AVAILABLE, "Colombo", VehicleType.CAR))
                .thenReturn(List.of(sampleDriver));

        List<DriverResponse> available = driverService.getAvailableDrivers("Colombo", VehicleType.CAR);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).getServiceArea()).isEqualTo("Colombo");
    }
}
