package com.re.rikkei_bank_manager.kyc.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycUploadRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    @NotNull(message = "Date of birth is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dob;
    
    @NotBlank(message = "Sex is required")
    private String sex;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "ID card image file is required")
    private MultipartFile file;
}
