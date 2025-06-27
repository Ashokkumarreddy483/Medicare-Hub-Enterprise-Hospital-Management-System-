package com.medicarehub.dto; // or com.medicarehub.dto.auth

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    @NotBlank
    private String usernameOrEmail; // Allow login with username or email

    @NotBlank
    private String password;
}