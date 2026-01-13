package com.dushy.tenantmanage.dto;

import com.dushy.tenantmanage.enums.IdProofType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDto {
    private String fullName;
    private String email;
    private String phone;
    private IdProofType idProofType;
    private String idProofNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDate moveInDate;
}
