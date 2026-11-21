package lk.driverandvehicleservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLicenseRequest {

    @NotBlank(message = "Driving license number is required")
    private String licenseNumber;

    @NotBlank(message = "License expiry date is required")
    private String expiryDate;

    @NotBlank(message = "License front photo URL is required")
    private String frontImageUrl;

    @NotBlank(message = "License back photo URL is required")
    private String backImageUrl;
}
