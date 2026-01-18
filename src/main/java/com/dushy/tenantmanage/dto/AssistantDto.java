package com.dushy.tenantmanage.dto;

import com.dushy.tenantmanage.enums.PropertyPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistantDto {
    private Long userId;
    private String email;
    private String fullName;
    private Set<PropertyPermission> permissions;
    private boolean isActive;
}
