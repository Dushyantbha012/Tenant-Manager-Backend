package com.dushy.tenantmanage.dto.request;

import com.dushy.tenantmanage.enums.PropertyPermission;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AddAssistantRequest {
    @NotEmpty(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotEmpty(message = "At least one permission is required")
    private Set<PropertyPermission> permissions;
}
