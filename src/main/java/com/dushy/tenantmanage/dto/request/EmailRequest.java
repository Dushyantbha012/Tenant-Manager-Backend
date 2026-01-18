package com.dushy.tenantmanage.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {
    @NotEmpty(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
