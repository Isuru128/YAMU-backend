package lk.accountservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    // Accepts either email address or Sri Lankan phone number
    @NotBlank(message = "Email or phone number is required")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;
}
