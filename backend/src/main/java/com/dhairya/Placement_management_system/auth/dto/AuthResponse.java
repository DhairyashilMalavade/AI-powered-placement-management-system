package com.dhairya.Placement_management_system.auth.dto;

import com.dhairya.Placement_management_system.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private UserResponse user;
}
