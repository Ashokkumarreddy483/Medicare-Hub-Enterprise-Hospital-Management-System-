package com.medicarehub.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DoctorRequestDto {

    // User details for the Doctor's account
    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email should be valid.")
    @Size(max = 80, message = "Email can be up to 80 characters.")
    private String email;

    // Password is required for creation, might be optional for update (handled in service)
    @Size(min = 6, max = 120, message = "Password must be between 6 and 120 characters.")
    private String password; // For creation. For update, this might be handled differently (e.g., separate endpoint or require current password)

    @NotBlank(message = "First name is required.")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Size(max = 50)
    private String lastName;

    @Size(max = 15, message = "Phone number can be up to 15 characters.")
    private String phoneNumber; // Optional for user

    // Doctor-specific details
    @NotNull(message = "Department ID is required.")
    private Long departmentId;

    @NotBlank(message = "Specialization is required.")
    @Size(max = 100)
    private String specialization;

    @NotBlank(message = "License number is required.")
    @Size(max = 50)
    private String licenseNumber;

    @PositiveOrZero(message = "Years of experience must be zero or positive.")
    private Integer yearsOfExperience;

    @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee must be zero or positive.")
    @Digits(integer = 8, fraction = 2, message = "Consultation fee format is invalid.") // e.g., 99999999.99
    private BigDecimal consultationFee;

    @Size(max = 500, message = "Qualifications can be up to 500 characters.")
    private String qualifications; // e.g., MBBS, MD (Cardiology)
}