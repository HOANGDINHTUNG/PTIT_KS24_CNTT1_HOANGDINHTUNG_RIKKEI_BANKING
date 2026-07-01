package com.re.rikkei_bank_manager.kyc.dto.response;

import com.re.rikkei_bank_manager.common.enums.KycStatus;
import lombok.*;
import java.time.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycResponse {
    private Long id;
    private String idNumber;
    private String fullName;
    private LocalDate dob;
    private String sex;
    private String address;
    private String idCardFrontUrl;
    private KycStatus status;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private Long userId;
    private String username;
}
