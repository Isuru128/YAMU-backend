package lk.accountservice.dto;

import jakarta.validation.constraints.NotNull;
import lk.accountservice.model.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private AccountStatus status;

    private String reason;
}
