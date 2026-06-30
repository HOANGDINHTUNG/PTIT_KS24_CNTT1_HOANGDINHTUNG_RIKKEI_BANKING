package com.re.rikkei_bank_manager.account.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class PinChangeRequest {
    @NotBlank private String oldPin;
    @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "must contain exactly 6 digits")
    private String newPin;
}
