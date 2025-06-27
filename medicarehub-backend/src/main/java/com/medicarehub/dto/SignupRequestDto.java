package com.medicarehub.dto; // or com.medicarehub.dto.auth

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class SignupRequestDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 3, max = 80)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 120)
    private String password;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;

    private Set<String> roles; // e.g., ["PATIENT"], ["ADMIN", "DOCTOR"]
}