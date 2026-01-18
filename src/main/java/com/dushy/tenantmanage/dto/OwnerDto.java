package com.dushy.tenantmanage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing an owner in the assistant mode.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDto {
    private Long id;
    private String fullName;
    private String email;
}
