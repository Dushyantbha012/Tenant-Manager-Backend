package com.dushy.tenantmanage.dto.auth;

import com.dushy.tenantmanage.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for JWT authentication response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long id;
    private String email;
    private String fullName;
    private UserType userType;
}
