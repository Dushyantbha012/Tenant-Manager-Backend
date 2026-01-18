package com.dushy.tenantmanage.dto.request;

import com.dushy.tenantmanage.enums.PropertyPermission;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UpdatePermissionsRequest {
    @NotEmpty(message = "At least one permission is required")
    private Set<PropertyPermission> permissions;
}
