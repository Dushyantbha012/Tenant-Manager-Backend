package com.dushy.tenantmanage.dto;

import com.dushy.tenantmanage.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private UserType userType;
}
